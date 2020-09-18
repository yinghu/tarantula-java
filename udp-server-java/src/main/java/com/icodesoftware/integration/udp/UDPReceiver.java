package com.icodesoftware.integration.udp;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

public class UDPReceiver implements Runnable {
    private DatagramChannel datagramChannel;
    private final String address;
    private final int port;
    //private final ConcurrentLinkedDeque<>
    public UDPReceiver(String address, int port){
        this.address = address;
        this.port = port;
    }
    @Override
    public void run(){
        System.out.println("WAITING FOR MESSAGE ...");
        while (true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                buffer.order(ByteOrder.BIG_ENDIAN);
                SocketAddress src = this.datagramChannel.receive(buffer);
                //datagramChannel.send(ByteBuffer.wrap("POP->".getBytes()),src);
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    public void start() throws Exception{
        this.datagramChannel = DatagramChannel.open();
        InetSocketAddress iAdd = new InetSocketAddress(address,port);
        this.datagramChannel.bind(iAdd);
    }
    public void stop() throws Exception{
        this.datagramChannel.close();
    }
}
