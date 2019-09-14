package com.tarantula.test;

import com.google.gson.JsonObject;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPListener implements Runnable{

    private DatagramChannel channel;
    private ByteBuffer readBuffer;
    private OnData onData;
    public UDPListener(OnData onData){
        this.onData = onData;
    }

    private void start() throws Exception{
        readBuffer = ByteBuffer.allocate(4096);
        channel = DatagramChannel.open();
        channel.bind(null);
    }
    public void connect(String host,int port) throws Exception{
        start();
        channel.connect(new InetSocketAddress(host,port));
    }
    public void message(JsonObject event) throws Exception{
        JsonObject message = new JsonObject();
        message.addProperty("command","onMessage");
        message.addProperty("applicationId",event.get("applicationId").getAsString());
        message.addProperty("instanceId",event.get("instanceId").getAsString());
        channel.write(ByteBuffer.wrap(message.toString().getBytes()));
    }
    public void join(String systemId,int stub,String instanceId,String ticket) throws Exception{
        JsonObject payload = new JsonObject();
        payload.addProperty("command","onJoin");
        payload.addProperty("systemId",systemId);
        payload.addProperty("instanceId",instanceId);
        payload.addProperty("stub",stub);
        payload.addProperty("ticket",ticket);
        ByteBuffer buffer = ByteBuffer.wrap(payload.toString().getBytes());
        channel.write(buffer);
    }
    public void leave(String systemId,String instanceId) throws Exception{
        JsonObject payload = new JsonObject();
        payload.addProperty("command","onLeave");
        payload.addProperty("systemId",systemId);
        payload.addProperty("instanceId",instanceId);
        ByteBuffer buffer = ByteBuffer.wrap(payload.toString().getBytes());
        channel.write(buffer);
    }
    @Override
    public void run() {
        try{
            while (true){
                readBuffer.clear();
                channel.receive(readBuffer);//use interrupt
                readBuffer.flip();
                byte[] data = new byte[readBuffer.limit()];
                readBuffer.get(data,0,data.length);
                onData.on(data);
            }
        }catch (Exception ex){
            //ignore
        }
    }
    public interface OnData{
        void on(byte[] data);
    }
}
