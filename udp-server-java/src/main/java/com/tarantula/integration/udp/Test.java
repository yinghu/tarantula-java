package com.tarantula.integration.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Test {



    public static void main(String[] args) throws Exception{
        DatagramChannel client = DatagramChannel.open();
        client.bind(null);
        ByteBuffer buffer = ByteBuffer.wrap("register".getBytes());
        InetSocketAddress serverAddress = new InetSocketAddress("10.0.0.234",9999);
        client.send(buffer, serverAddress);
        ByteBuffer pending = ByteBuffer.allocate(4096);
        while (true){
            pending.clear();
            client.receive(pending);
            pending.flip();
            int limits = pending.limit();
            byte bytes[] = new byte[limits];
            pending.get(bytes, 0, limits);
            String rec = new String(bytes);
            System.out.println("server at " +serverAddress+ "  sent: " + rec);
        }
        //client.close();
    }
}
