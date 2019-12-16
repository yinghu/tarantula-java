package com.tarantula.integration.udp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* Service Connector the single connection to the gec cluster to receive updates and publish those updates via udp package.
* The connection should not write data from udp to gec cluster
* */
public class ServiceConnector implements Runnable {

    private static Logger log = Logger.getLogger(ServiceConnector.class.getName());
    private static int READ_BUFFER_SIZE = 4096;
    private JsonObject config;
    private SocketChannel socketChannel;
    private String serverId;
    private ByteBuffer readBuffer;
    private ConcurrentLinkedDeque<OutboundMessage> outboundQueue;
    private JsonParser jsonParser;
    private StringBuilder pending;
    private UDPServer udpServer;
    public ServiceConnector(){
        this.serverId = UUID.randomUUID().toString();
    }
    public void start() throws Exception{
        jsonParser = new JsonParser();
        File f = new File("/etc/tarantula/udp.conf");
        if(f.exists()){
            config = jsonParser.parse(new InputStreamReader(new FileInputStream(f))).getAsJsonObject();
        }
        else{
            config = jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("udp.conf"))).getAsJsonObject();
        }
        File ipf = new File("ip.txt");
        if(ipf.exists()){
            //REPLACE UDP FRONT BINDING
            BufferedReader r = new BufferedReader(new FileReader(ipf));
            config.getAsJsonObject("front").remove("host");
            String ip = r.readLine();
            r.close();
            log.warning("Front binding replaced with ip ["+ip+"]");
            config.getAsJsonObject("front").addProperty("host",ip);
        }
        this.outboundQueue = new ConcurrentLinkedDeque<>();
        this.readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
        this.pending = new StringBuilder();
        while(true){
            try{
                socketChannel = SocketChannel.open();
                JsonObject conn = config.get("server").getAsJsonObject().get("connection").getAsJsonObject();
                socketChannel.connect(new InetSocketAddress(conn.get("host").getAsString(),conn.get("port").getAsInt()));
                onRegister();
                break;
            }catch (Exception ex){
                log.warning("waiting 5 seconds to connect again");
                Thread.sleep(5000);
            }
        }
        udpServer = new UDPServer(config.get("front").getAsJsonObject(),outboundQueue,this);
        udpServer.start();
    }
    private void _restart(){
        while(true){
            try{
                socketChannel = SocketChannel.open();
                JsonObject conn = config.get("server").getAsJsonObject().get("connection").getAsJsonObject();
                socketChannel.connect(new InetSocketAddress(conn.get("host").getAsString(),conn.get("port").getAsInt()));
                onRegister();
                log.info("UDP server has reconnected to platform ["+conn.toString()+"]");
                break;
            }catch (Exception ex){
                log.log(Level.WARNING,"failed to reconnect to platform");
                try{Thread.sleep(5000);}catch (Exception tex){}
            }
        }
    }
    public void stop(){
        log.warning("Udp shut down");
        udpServer.stop();
    }
    public void onTicket(String systemId, int stub, String ticket, HTTPCaller.OnResponse onResponse){
        HashMap<String,String> _headers = new HashMap<>();
        _headers.put(HTTPCaller.TARANTULA_TAG,"index/user");
        _headers.put(HTTPCaller.TARANTULA_MAGIC_KEY,systemId);
        JsonObject web = config.get("server").getAsJsonObject().get("web").getAsJsonObject();
        HTTPCaller http = new HTTPCaller(false,web.get("host").getAsString()+":"+web.get("port").getAsInt());
        JsonObject payload = new JsonObject();
        payload.addProperty("stub",stub);
        payload.addProperty("accessKey",ticket);
        http.doAction("user/action","onTicket",_headers,payload.toString().getBytes(),onResponse);
    }
    @Override
    public void run() {
        //message format from server push event
        /**
         *  [clientId],[label]#[instanceId]?[query][json payload]
         * */
        OutboundMessage outboundMessage = new OutboundMessage();
        while(true){
            try{
                readBuffer.clear();
                int rn = socketChannel.read(readBuffer);//block read
                if(rn>0){
                    readBuffer.flip();
                    while(readBuffer.hasRemaining()){
                        char c = (char) readBuffer.get();
                            if(outboundMessage.onHeader&&c == ','){
                                outboundMessage.clientId = pending.toString();
                                pending.setLength(0);
                            }
                            else if(outboundMessage.onHeader&&c=='#'){
                                outboundMessage.label =pending.toString();
                                pending.setLength(0);
                            }
                            else if(outboundMessage.onHeader&&c==UDPServer.MSG_HEADER_DELIMITER){
                                outboundMessage.instanceId = pending.toString();
                                outboundMessage.onQuery = true;
                                pending.setLength(0);
                            }
                            else if(outboundMessage.onHeader&&c=='{'){
                                if(outboundMessage.onQuery){
                                    outboundMessage.query = pending.toString();
                                }
                                else{
                                    outboundMessage.query ="onUnknown";
                                    outboundMessage.instanceId = pending.toString();
                                }
                                pending.setLength(0);
                                outboundMessage.onHeader = false;
                            }
                            if(c=='|'){
                                outboundMessage.data = pending.toString();
                                outboundQueue.offer(outboundMessage);
                                pending.setLength(0);
                                outboundMessage = new OutboundMessage();
                                continue;
                            }
                            if((outboundMessage.onHeader&&(c==','||c=='#'))||c==UDPServer.MSG_HEADER_DELIMITER){
                                continue;
                            }
                            pending.append(c);
                    }
                }
                else{
                    Thread.sleep(50);
                }
            }catch (Exception ex){
                log.log(Level.WARNING,"reconnecting from disconnected platform",ex);
                _restart();
            }
        }
    }

    private void onRegister() throws Exception{
        JsonObject payload = new JsonObject();
        payload.addProperty("action","onConnect");
        payload.addProperty("clientId","push/streaming");
        payload.addProperty("path","/push/action");
        payload.addProperty("streaming",true);
        payload.addProperty("serverId",serverId);
        JsonObject jsonObject = new JsonObject();
        payload.add("data",jsonObject);
        JsonObject front = config.get("front").getAsJsonObject();
        jsonObject.addProperty("command","onConnect");
        jsonObject.addProperty("type",front.get("type").getAsString());
        jsonObject.addProperty("secured",front.get("secured").getAsBoolean());
        jsonObject.addProperty("protocol","udp");
        jsonObject.addProperty("host",front.get("host").getAsString());
        jsonObject.addProperty("port",front.get("port").getAsInt());
        jsonObject.addProperty("path",front.get("path").getAsString());
        jsonObject.addProperty("maxConnections",front.get("maxConnections").getAsInt());
        jsonObject.addProperty("serverId",serverId);
        ByteBuffer buffer = ByteBuffer.wrap((payload.toString()+"|").getBytes());
        socketChannel.write(buffer);
    }

}
