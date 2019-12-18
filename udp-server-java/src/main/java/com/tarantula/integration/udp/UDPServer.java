package com.tarantula.integration.udp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

public class UDPServer implements Runnable {

    private static Logger log = Logger.getLogger(UDPServer.class.getName());
    private static int MAX_PAYLOAD_SIZE = 4096;
    static char MSG_HEADER_DELIMITER = '?';
    private JsonObject front;
    private DatagramChannel uchannel;
    private ConcurrentHashMap<String,SessionGroup> cMap;
    private ConcurrentLinkedDeque<OutboundMessage> oQueue;
    private ServiceConnector serviceConnector;
    private Thread tu;
    private Thread tr;
    public UDPServer(JsonObject front, ConcurrentLinkedDeque<OutboundMessage> mQueue,ServiceConnector serviceConnector){
        this.front = front;
        MSG_HEADER_DELIMITER = front.get("messageHeaderDelimiter").getAsCharacter();
        this.oQueue = mQueue;
        this.serviceConnector = serviceConnector;
    }
    public void start() throws Exception{
        cMap = new ConcurrentHashMap<>();
        uchannel = DatagramChannel.open();
        String host = front.get("binding").getAsString();
        int port = front.get("port").getAsInt();
        InetSocketAddress uAdd = new InetSocketAddress(host,port);
        uchannel.bind(uAdd);
        ByteBuffer outBuffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
        JsonParser parser = new JsonParser();
        tu = new Thread(this,"tarantula-udp-server");
        tu.start();
        tr = new Thread(()->{
            while (true){
                try{
                    OutboundMessage m = oQueue.poll();
                    if(m!=null){
                        try{
                            if(m.instanceId!=null){//skip notifications
                                SessionGroup sg = cMap.computeIfAbsent(m.instanceId,k->new SessionGroup(k));
                                if(m.query.equals("onTimeout")){
                                    JsonObject jt = parser.parse(m.data).getAsJsonObject();
                                    sg.sessions.remove(new Session(jt.get("systemId").getAsString()));
                                }
                                outBuffer.clear();
                                outBuffer.put(m.label.getBytes());
                                outBuffer.put((byte)'#');
                                outBuffer.put(m.instanceId.getBytes());
                                outBuffer.put((byte)'?');
                                outBuffer.put(m.query.getBytes());
                                outBuffer.put(m.data.getBytes());
                                //outBuffer.flip();
                                sg.sessions.forEach(s->{
                                    outBuffer.flip();
                                    try{uchannel.send(outBuffer,s.endpoint);}catch (Exception iexc){iexc.printStackTrace();}
                                });
                                if(m.query.equals("onEnd")){//removed
                                    cMap.remove(m.instanceId);
                                }
                            }
                        }catch (Exception iex){
                            iex.printStackTrace();
                        }
                    }
                    else{
                        Thread.sleep(100);
                    }
                }catch (Exception ex){
                    //ignore ex
                }
            }
        },"tarantula-outbound-sender");
        tr.start();
        log.warning("Tarantula UDP server is listening at  ["+host+":"+port+"]");
    }
    public void stop(){
        log.warning("udp server is down");
        tu.interrupt();
        tr.interrupt();
    }
    @Override
    public void run(){
        ByteBuffer buffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
        JsonParser parser = new JsonParser();
        while (true){
            try{
                buffer.clear();
                SocketAddress remoteAdd = uchannel.receive(buffer);//from udp client
                buffer.flip();
                parse(buffer,(outboundMessage) -> {
                     if(outboundMessage.query.equals("onMessage")){
                        //handle
                        buffer.clear();
                        buffer.put(outboundMessage.label.getBytes());
                        buffer.put((byte)'#');
                        buffer.put(outboundMessage.instanceId.getBytes());
                        buffer.put((byte)'?');
                        buffer.put(outboundMessage.query.getBytes());
                        buffer.put(outboundMessage.data.getBytes());
                        SessionGroup sg = cMap.computeIfAbsent(outboundMessage.instanceId,k->new SessionGroup(k));
                        sg.sessions.forEach(s->{
                            buffer.flip();
                            try{uchannel.send(buffer,s.endpoint);}catch (Exception iex){iex.printStackTrace();}
                        });
                    }
                    else if(outboundMessage.query.equals("onJoin")){
                        JsonObject jsonObject = parser.parse(outboundMessage.data).getAsJsonObject();
                        String insId = jsonObject.get("instanceId").getAsString();
                        String systemId = jsonObject.get("systemId").getAsString();
                        int stub = jsonObject.get("stub").getAsInt();
                        String ticket = jsonObject.get("ticket").getAsString();
                        serviceConnector.onTicket(systemId,stub,ticket,(c,resp)->{
                            boolean suc = resp.get("successful").getAsBoolean();
                            if(suc){
                                String sysId= resp.get("presence").getAsJsonObject().get("systemId").getAsString();
                                String token= resp.get("presence").getAsJsonObject().get("token").getAsString();
                                SessionGroup sg = cMap.computeIfAbsent(insId,k->new SessionGroup(insId));
                                sg.sessions.add(new Session(sysId,stub,token,remoteAdd));
                            }
                            //send back as ticket validation result
                            buffer.clear();
                            buffer.put(outboundMessage.label.getBytes());
                            buffer.put((byte)'#');
                            buffer.put(insId.getBytes());
                            buffer.put((byte)'?');
                            buffer.put(suc?"joined".getBytes():"failed".getBytes());
                            buffer.put("{}".getBytes());
                            buffer.flip();
                            try{uchannel.send(buffer,remoteAdd);}catch (Exception iex){iex.printStackTrace();}
                        });
                    }
                    else{
                        log.warning("query>>>"+outboundMessage.query);
                    }

                });
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    private void parse(ByteBuffer buffer, OutboundMessage.OnResponse onResponse){
            //message format from UDP client
            /**
             *  [label]#[instanceId]?[query][json payload]
             * */
            OutboundMessage pendingData = new OutboundMessage();
            boolean p =false;
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<buffer.limit();i++){
                char c = (char) buffer.get();
                if(!p&&c=='#'){
                    pendingData.label = sb.toString();
                    sb.setLength(0);
                }
                else if(!p&&c==MSG_HEADER_DELIMITER){
                    pendingData.instanceId = sb.toString();
                    pendingData.onQuery = true;
                    sb.setLength(0);
                }
                else if(!p&&c=='{'){
                    if(pendingData.onQuery){
                        pendingData.query = sb.toString();
                    }else{
                        pendingData.instanceId = sb.toString();
                        pendingData.query ="onUnknown";
                    }
                    sb.setLength(0);
                    p = true;
                }
                if(c=='|'){
                    break;
                }
                if((!p&&(c=='#'||c==MSG_HEADER_DELIMITER))){
                    continue;
                }
                sb.append(c);
            }
            pendingData.data = (sb.toString());
            onResponse.on(pendingData);
    }
}
