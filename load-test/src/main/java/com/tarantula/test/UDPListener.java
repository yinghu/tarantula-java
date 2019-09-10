package com.tarantula.test;

import com.google.gson.JsonObject;
import com.tarantula.test.integration.OnPayload;

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

    public void start() throws Exception{
        readBuffer = ByteBuffer.allocate(4096);
        channel = DatagramChannel.open();
        channel.bind(null);
    }
    public void connect(String host,int port) throws Exception{
        channel.connect(new InetSocketAddress(host,port));
        JsonObject payload = new JsonObject();
        payload.addProperty("systemId","BDS01/1234455");
        payload.addProperty("stub",4);
        payload.addProperty("ticket","ticket-0001");
        ByteBuffer buffer = ByteBuffer.wrap(payload.toString().getBytes());
        channel.write(buffer);
    }
    public static void main(String[] args) throws Exception{
        UDPListener udp = new UDPListener(data -> {
            System.out.println(new String(data));
        });
        udp.start();
        udp.connect("10.0.0.234",9999);
        udp.run();
    }

    @Override
    public void run() {
        try{
            while (true){
                readBuffer.clear();
                channel.receive(readBuffer);
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
