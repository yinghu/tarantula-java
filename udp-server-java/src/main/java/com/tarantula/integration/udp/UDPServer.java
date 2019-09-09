package com.tarantula.integration.udp;

import com.google.gson.JsonObject;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

public class UDPServer implements Runnable {

    private static Logger log = Logger.getLogger(UDPServer.class.getName());

    private JsonObject front;
    private DatagramChannel uchannel;
    private ConcurrentHashMap<String,SocketAddress> cMap;
    private ConcurrentLinkedDeque<OutboundMessage> oQueue;

    public UDPServer(JsonObject front, ConcurrentLinkedDeque<OutboundMessage> mQueue){
        this.front = front;
        this.oQueue = mQueue;
    }
    public void start() throws Exception{
        cMap = new ConcurrentHashMap<>();
        uchannel = DatagramChannel.open();
        InetSocketAddress uAdd = new InetSocketAddress(front.get("host").getAsString(), front.get("port").getAsInt());
        uchannel.bind(uAdd);
        new Thread(this,"tarantula-udp-server").start();
        new Thread(()->{
            while (true){
                try{
                    OutboundMessage m = oQueue.poll();
                    if(m!=null){
                        cMap.forEach((k,v)->{//broadcasting
                            try{
                                ByteBuffer b = ByteBuffer.wrap(m.data.getBytes());
                                uchannel.send(b,v);
                            }catch (Exception iex){}
                        });
                        //log.warning(m.toString());
                    }
                    else{
                        Thread.sleep(100);
                    }
                }catch (Exception ex){
                    //ignore ex
                }
            }
        },"tarantula-outbound-sender").start();
    }
    @Override
    public void run(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true){
            try{
                buffer.clear();
                SocketAddress remoteAdd = uchannel.receive(buffer);
                log.warning("remote from "+remoteAdd.toString());
                cMap.put(UUID.randomUUID().toString(),remoteAdd);
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
}
