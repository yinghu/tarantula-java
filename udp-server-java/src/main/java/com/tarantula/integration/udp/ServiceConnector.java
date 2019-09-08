package com.tarantula.integration.udp;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class ServiceConnector implements Runnable {
    private SocketChannel socketChannel;
    private String serverId;
    public ServiceConnector(){
        this.serverId = UUID.randomUUID().toString();
    }
    public void start() throws Exception{
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost",6393));
        onRegister();
        Thread.sleep(10000);
        socketChannel.close();
    }
    public void write() throws Exception{

    }
    @Override
    public void run() {
        while(true){
            try{

            }catch (Exception ex){

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
        jsonObject.addProperty("command","onConnect");
        jsonObject.addProperty("type","udp");
        jsonObject.addProperty("secured",false);
        jsonObject.addProperty("protocol","udp");
        jsonObject.addProperty("host","localhost");
        jsonObject.addProperty("port",9999);
        jsonObject.addProperty("path","tarantula");
        jsonObject.addProperty("serverId",serverId);
        System.out.println(payload.toString().length());
        ByteBuffer buffer = ByteBuffer.wrap((payload.toString()+"|").getBytes());
        socketChannel.write(buffer);
    }

}
