package com.tarantula.integration.udp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
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
    private static int MAX_PAYLOAD_SIZE = 4096;
    private JsonObject front;
    private DatagramChannel uchannel;
    private ConcurrentHashMap<String,SocketAddress> cMap;
    private ConcurrentLinkedDeque<OutboundMessage> oQueue;
    private ServiceConnector serviceConnector;
    private JsonParser parser;
    public UDPServer(JsonObject front, ConcurrentLinkedDeque<OutboundMessage> mQueue,ServiceConnector serviceConnector){
        this.front = front;
        this.oQueue = mQueue;
        this.serviceConnector = serviceConnector;
        this.parser = new JsonParser();
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
        ByteBuffer buffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
        while (true){
            try{
                buffer.clear();
                SocketAddress remoteAdd = uchannel.receive(buffer);
                buffer.flip();
                byte[] data = new byte[buffer.limit()];
                buffer.get(data,0,data.length);
                parse(data,jsonObject -> {
                    if(serviceConnector.onTicket(jsonObject.get("systemId").getAsString(),jsonObject.get("stub").getAsInt(),jsonObject.get("ticket").getAsString())){
                        cMap.put(jsonObject.get("systemId").getAsString(),remoteAdd);
                    }
                });
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    private void parse(byte[] data, HTTPCaller.OnResponse onResponse){
        JsonObject jsonObject = new JsonObject();
        try{
            jsonObject = parser.parse(new InputStreamReader(new ByteArrayInputStream(data))).getAsJsonObject();
            onResponse.on(jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            jsonObject.addProperty("error",ex.getMessage());
        }
    }
}
