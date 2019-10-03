package com.tarantula.integration.udp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
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

    private JsonObject config;
    private SocketChannel socketChannel;
    private String serverId;
    private ByteBuffer readBuffer;
    private ConcurrentLinkedDeque<OutboundMessage> outboundQueue;
    private JsonParser jsonParser;
    private PendingData pending;
    private UDPServer udpServer;
    public ServiceConnector(){
        this.serverId = UUID.randomUUID().toString();
    }
    public void start() throws Exception{
        jsonParser = new JsonParser();
        config = jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("udp.conf"))).getAsJsonObject();
        this.outboundQueue = new ConcurrentLinkedDeque<>();
        this.readBuffer = ByteBuffer.allocate(1024);
        this.pending = new PendingData();
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
        while(true){
            try{
                readBuffer.clear();
                int rn = socketChannel.read(readBuffer);//block read
                if(rn>0){
                    readBuffer.flip();
                    while(readBuffer.hasRemaining()){
                        char c = (char) readBuffer.get();
                        if(!pending.onData){
                            if(!pending.onLabel&&c != ','){
                                pending.clientId.append(c);
                            }
                            else if(!pending.onLabel&&c==','){
                                pending.onLabel = true;
                            }
                            else if(pending.onLabel&&c!='{'){
                                pending.label.append(c);
                                pending.data.append(c);
                            }
                            else{
                                pending.data.append(c);
                                pending.onData = true;
                            }
                        }
                        else{
                            if(c=='|'){
                                OutboundMessage out = pending.reset();
                                if(out.label.startsWith("timeout")){
                                    JsonObject jo = jsonParser.parse(out.data.substring(7)).getAsJsonObject();
                                    udpServer.onTimeout(jo.get("systemId").getAsString(),jo.get("instanceId").getAsString());
                                }else{
                                    outboundQueue.offer(out);//dispatch
                                }
                            }
                            else{
                                pending.data.append(c);
                            }
                        }
                    }
                }
                else{
                    Thread.sleep(10);
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
