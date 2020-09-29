package com.icodesoftware.integration.udp;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPReceiver implements Runnable {
    private DatagramChannel datagramChannel;
    private final String address;
    private final int port;
    private final ConcurrentLinkedDeque<PendingInboundMessage> mQueue;
    private ExecutorService executorService;
    public UDPReceiver(String address, int port){
        this.address = address;
        this.port = port;
        mQueue = new ConcurrentLinkedDeque<>();
    }
    @Override
    public void run(){
        System.out.println("WAITING FOR MESSAGE ...");
        while (true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(PendingOutboundMessage.MESSAGE_SIZE);
                SocketAddress src = this.datagramChannel.receive(buffer);
                PendingInboundMessage inboundMessage = new PendingInboundMessage("",buffer,src);
                mQueue.offer(inboundMessage);
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
        executorService = Executors.newFixedThreadPool(3);
        executorService.execute(()->{

        });
    }
    public void stop() throws Exception{
        this.datagramChannel.close();
    }
}
