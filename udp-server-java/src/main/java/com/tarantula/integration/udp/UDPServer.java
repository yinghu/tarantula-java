package com.tarantula.integration.udp;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPServer extends Thread {
    @Override
    public void run(){
        try{
            DatagramChannel server = DatagramChannel.open();
            InetSocketAddress iAdd = new InetSocketAddress("localhost", 8989);
            server.bind(iAdd);
            System.out.println("Server Started: " + iAdd);
            while (true){
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //receive buffer from client.
            SocketAddress remoteAdd = server.receive(buffer);
            //change mode of buffer
            buffer.flip();
            int limits = buffer.limit();
            byte bytes[] = new byte[limits];
            buffer.get(bytes, 0, limits);
            String msg = new String(bytes);
            System.out.println("Client at " + remoteAdd + "  sent: " + msg);
            buffer.rewind();
            server.send(buffer,remoteAdd);
            }
            //server.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void _send() throws Exception {
        DatagramChannel client = null;
        client = DatagramChannel.open();

        client.bind(null);

        String msg = "Hello World!";
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        InetSocketAddress serverAddress = new InetSocketAddress("localhost",
                8989);

        client.send(buffer, serverAddress);
        buffer.clear();
        client.receive(buffer);
        buffer.flip();
        int limits = buffer.limit();
        byte bytes[] = new byte[limits];
        buffer.get(bytes, 0, limits);
        String rec = new String(bytes);
        System.out.println("server at " +serverAddress+ "  sent: " + rec);
        client.close();
    }
}
