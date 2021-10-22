package com.icodesoftware.protocol;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.handler.MessageHandlerHeader;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TarantulaThreadFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPEndpointService implements Runnable, Serviceable,Messenger {

    private static TarantulaLogger log = JDKLogger.getLogger(UDPEndpointService.class);
    private static int BUFFER_SIZE = 508;
    private static int PORT = 11933;
    private static int MESSAGE_HANDLER_POOL_SIZE = 3;

    private DatagramSocket datagramChannel;

    private HashMap<Integer,MessageHandler> messageHandlers = new HashMap<>();

    private ConcurrentLinkedDeque<DatagramPacket> pendingMessageQueue = new ConcurrentLinkedDeque();
    private ExecutorService executorService;

    private MessageHandlerHeader messageHandlerHeader;
    private ConcurrentHashMap<Integer,UserChannel> userChannelIndex = new ConcurrentHashMap<>();
    private String host;

    public UDPEndpointService(JsonObject config){
        this.host = config.getAsJsonObject("connection").get("host").getAsString();
        this.userChannelIndex.put(1,new UserChannel(1,this));
    }

    public void start() throws Exception{
        this.messageHandlerHeader = new MessageHandlerHeader(1,this);
        executorService = Executors.newFixedThreadPool(MESSAGE_HANDLER_POOL_SIZE,new TarantulaThreadFactory("messaging"));
        for(int i=0;i<MESSAGE_HANDLER_POOL_SIZE;i++){
            executorService.execute(()->{
                MessageBuffer messageBuffer = new MessageBuffer();
                while(true){
                    try{
                        DatagramPacket packet = pendingMessageQueue.poll();
                        if(packet!=null){
                            byte[] data = Arrays.copyOf(packet.getData(),packet.getLength());
                            messageBuffer.reset(data);
                            MessageBuffer.MessageHeader messageHeader = messageBuffer.readHeader();
                            UserChannel userChannel = userChannelIndex.get(messageHeader.channelId);
                            userChannel.onMessage(messageHeader,messageBuffer,packet.getSocketAddress());
                        }
                        else{
                            Thread.sleep(5);
                        }
                    }catch (Exception ex){
                        //ignore
                        ex.printStackTrace();
                    }
                }
            });
        }
        this.datagramChannel = new DatagramSocket(null);
        InetSocketAddress addr = new InetSocketAddress(host,PORT);
        this.datagramChannel.bind(addr);
    }
    public void shutdown() throws Exception{
        log.warn("UDP endpoint service is going down");
        this.datagramChannel.close();
    }

    @Override
    public void run() {
        log.warn("UDP endpoint service is ready on ["+host+": 11933]");
        while (true){
            try{
                DatagramPacket buffer = new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
                this.datagramChannel.receive(buffer);
                pendingMessageQueue.offer(buffer);
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
                //try{Thread.sleep(50);}catch (Exception exx){}
            }
        }
    }

    @Override
    public void send(MessageBuffer messageBuffer, SocketAddress destination) {
        try {
            byte[] data = messageBuffer.toArray();
            DatagramPacket packet = new DatagramPacket(data,data.length,destination);
            this.datagramChannel.send(packet);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
