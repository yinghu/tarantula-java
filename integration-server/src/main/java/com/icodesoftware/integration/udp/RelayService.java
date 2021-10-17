package com.icodesoftware.integration.udp;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashSet;


public class RelayService implements Runnable{

    private static TarantulaLogger log = JDKLogger.getLogger(RelayService.class);
    private static int BUFFER_SIZE = 576;
    private static int PORT = 11933;

    private DatagramSocket datagramChannel;
    private HashSet<SocketAddress> socketAddresses = new HashSet<>();
    private String host;
    public RelayService(String host){
        this.host = host;
    }

    public void start() throws Exception{
        this.datagramChannel = new DatagramSocket(null);
        InetSocketAddress addr = new InetSocketAddress(host,PORT);
        this.datagramChannel.bind(addr);
    }
    public void shutdown() throws Exception{
        log.warn("Relay service is going down");
        this.datagramChannel.close();
    }

    @Override
    public void run() {
        //String _app = application.substring(application.lastIndexOf(".")+1);
        log.warn("Relay service is ready on ["+host+": 11933]");
        while (true){
            try{
                DatagramPacket buffer = new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
                this.datagramChannel.receive(buffer);
                SocketAddress source = buffer.getSocketAddress();
                socketAddresses.add(source);
                byte[] payload = Arrays.copyOf(buffer.getData(),buffer.getLength());
                socketAddresses.forEach((s)->{
                    try{
                        if(!s.equals(source)) this.datagramChannel.send(new DatagramPacket(payload, payload.length,s));
                    }
                    catch (IOException ioex){
                        ioex.printStackTrace();
                    }
                });
                //mQueue.offer(new PendingMessage(payload,buffer.getSocketAddress()));
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
                //try{Thread.sleep(50);}catch (Exception exx){}
            }
        }
    }
}
