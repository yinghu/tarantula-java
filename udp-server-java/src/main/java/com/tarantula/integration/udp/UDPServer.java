package com.tarantula.integration.udp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
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
    private JsonObject front;
    private DatagramChannel uchannel;
    private ConcurrentHashMap<Session,SocketAddress> cMap;
    private ConcurrentLinkedDeque<OutboundMessage> oQueue;
    private ServiceConnector serviceConnector;
    private JsonParser parser;
    private Thread tu;
    private Thread tr;
    public UDPServer(JsonObject front, ConcurrentLinkedDeque<OutboundMessage> mQueue,ServiceConnector serviceConnector){
        this.front = front;
        this.oQueue = mQueue;
        this.serviceConnector = serviceConnector;
        this.parser = new JsonParser();
    }
    public void start() throws Exception{
        cMap = new ConcurrentHashMap<>();
        uchannel = DatagramChannel.open();
        String host = front.get("host").getAsString();
        int port = front.get("port").getAsInt();
        InetSocketAddress uAdd = new InetSocketAddress(host,port);
        uchannel.bind(uAdd);
        ByteBuffer outBuffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
        tu = new Thread(this,"tarantula-udp-server");
        tu.start();
        tr = new Thread(()->{
            while (true){
                try{
                    OutboundMessage m = oQueue.poll();
                    if(m!=null){
                        cMap.forEach((k,v)->{//broadcasting
                            try{
                                String[] mh = m.label.split("#");
                                if(mh[1].equals(k.instanceId)){
                                    outBuffer.clear();
                                    outBuffer.put(m.data.getBytes());
                                    outBuffer.flip();
                                    uchannel.send(outBuffer,v);
                                }
                            }catch (Exception iex){}
                        });
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
    public void onTimeout(String systemId,String instanceId){
        log.warning("LEAVE FROM->"+systemId+"<><>"+instanceId);
        cMap.remove(new Session(systemId,instanceId));
    }
    @Override
    public void run(){
        ByteBuffer buffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
        while (true){
            try{
                buffer.clear();
                SocketAddress remoteAdd = uchannel.receive(buffer);//from udp client
                buffer.flip();
                byte[] data = new byte[buffer.limit()];
                buffer.get(data,0,data.length);
                parse(data,(cmd,jsonObject) -> {
                    if(cmd.equals("onJoin")){
                        String insId = jsonObject.get("instanceId").getAsString();
                        String systemId = jsonObject.get("systemId").getAsString();
                        int stub = jsonObject.get("stub").getAsInt();
                        String ticket = jsonObject.get("ticket").getAsString();
                        serviceConnector.onTicket(systemId,stub,ticket,(c,resp)->{
                            if(resp.get("successful").getAsBoolean()){
                                String sysId= resp.get("presence").getAsJsonObject().get("systemId").getAsString();
                                String token= resp.get("presence").getAsJsonObject().get("token").getAsString();
                                cMap.put(new Session(sysId,stub,insId,token),remoteAdd);
                            }
                            //send back as ticket validation result
                            buffer.clear();
                            buffer.put("ticket".getBytes());
                            buffer.put(resp.toString().getBytes());
                            buffer.flip();
                            try{uchannel.send(buffer,remoteAdd);}catch (Exception iex){iex.printStackTrace();}
                        });
                    }
                    else if(cmd.equals("onLeave")){
                        String sysId = jsonObject.get("systemId").getAsString();
                        String insId = jsonObject.get("instanceId").getAsString();
                        this.onTimeout(sysId,insId);
                    }
                    else if(cmd.equals("onMessage")){
                        log.warning(jsonObject.toString());
                        //handle
                        buffer.clear();
                        buffer.put(jsonObject.get("label").getAsString().getBytes());
                        buffer.put(jsonObject.get("data").toString().getBytes());
                        cMap.forEach((k,v)->{
                            if(k.instanceId.equals(jsonObject.get("instanceId").getAsString())){
                                buffer.flip();
                                try{uchannel.send(buffer,remoteAdd);}catch (Exception iex){iex.printStackTrace();}
                            }
                        });
                    }
                });
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    private void parse(byte[] data, HTTPCaller.OnResponse onResponse){
        JsonObject jsonObject = new JsonObject();
        try{
            jsonObject = parser.parse(new InputStreamReader(new ByteArrayInputStream(data))).getAsJsonObject();
            String cmd = jsonObject.get("command").getAsString();
            onResponse.on(cmd,jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            jsonObject.addProperty("error",ex.getMessage());
        }
    }
}
