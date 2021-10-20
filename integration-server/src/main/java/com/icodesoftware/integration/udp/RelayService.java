package com.icodesoftware.integration.udp;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;

public class RelayService implements Runnable{

    private static TarantulaLogger log = JDKLogger.getLogger(RelayService.class);
    private static int BUFFER_SIZE = 508;
    private static int PORT = 11933;

    private DatagramSocket datagramChannel;
    private HashMap<String,RemoteClient> socketAddresses = new HashMap<>();
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
        MessageBuffer messageBuffer = new MessageBuffer();
        while (true){
            try{
                DatagramPacket buffer = new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
                this.datagramChannel.receive(buffer);
                SocketAddress source = buffer.getSocketAddress();
                String[] ip = source.toString().split(":");
                RemoteClient rc = socketAddresses.get(ip[0]);
                if(rc==null){
                    socketAddresses.put(ip[0],new RemoteClient(ip[1],source));
                }
                else if(rc!=null && !rc.suffix.equals(ip[1])){
                    socketAddresses.replace(ip[0],new RemoteClient(ip[1],source));
                }
                byte[] payload = Arrays.copyOf(buffer.getData(),buffer.getLength());
                messageBuffer.reset(payload);
                MessageBuffer.MessageHeader header = messageBuffer.readHeader();
                socketAddresses.forEach((k,s)->{
                    try{
                        if(header.broadcasting){
                            this.datagramChannel.send(new DatagramPacket(payload,payload.length,s.socketAddress));
                        }
                        else if(!k.equals(ip[0])){
                            this.datagramChannel.send(new DatagramPacket(payload,payload.length,s.socketAddress));
                        }
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
    private class RemoteClient{
        public String suffix;
        public SocketAddress socketAddress;

        public RemoteClient(String suffix,SocketAddress socketAddress){
            this.suffix = suffix;
            this.socketAddress = socketAddress;
        }
    }
}
