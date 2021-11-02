package com.icodesoftware.protocol;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.icodesoftware.util.TarantulaThreadFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPEndpointService implements UDPEndpointServiceProvider {

    private static TarantulaLogger log = JDKLogger.getLogger(UDPEndpointService.class);
    private static int BUFFER_SIZE = 508;
    private static int PORT = 11933;
    private static int BACK_LOG = 100;
    private static int MESSAGE_HANDLER_POOL_SIZE = 3;

    private DatagramSocket datagramChannel;

    private ConcurrentLinkedDeque<DatagramPacket> pendingMessageQueue = new ConcurrentLinkedDeque();

    private ConcurrentHashMap<Integer,UserChannel> userChannelIndex = new ConcurrentHashMap<>();

    private String host;
    private int port = PORT;
    private int backlog = BACK_LOG;

    private ExecutorService executorService;
    private String inboundThreadPoolSetting;
    private int messageHandlerSize = MESSAGE_HANDLER_POOL_SIZE;
    private boolean daemon;

    public void start() throws Exception{
        if(inboundThreadPoolSetting!=null){
            TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool, poolSize, rh)->{
                this.executorService = pool;
                this.messageHandlerSize = poolSize-(daemon?2:1);
            });
        }else{
            executorService = Executors.newFixedThreadPool(MESSAGE_HANDLER_POOL_SIZE+(daemon?2:1),new TarantulaThreadFactory("udp-messaging"));
        }
        for(int i=0;i<messageHandlerSize;i++){
            executorService.execute(()->{
                MessageBuffer messageBuffer = new MessageBuffer();
                while(true){
                    try{
                        DatagramPacket packet = pendingMessageQueue.poll();
                        if(packet!=null){
                            byte[] data = Arrays.copyOf(packet.getData(),packet.getLength());
                            messageBuffer.reset(data);
                            messageBuffer.flip();
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
        executorService.execute(()->{
            long kickoffTimer = 5000;
            while (true){
                try{
                    Thread.sleep(200);
                    userChannelIndex.forEach((k,v)->v.onRetry());
                    kickoffTimer -= 200;
                    if(kickoffTimer<=0){
                        userChannelIndex.forEach((k,v)->v.onKickoff());
                        kickoffTimer = 5000;
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        this.datagramChannel = new DatagramSocket(null);
        InetSocketAddress addr = host!=null?new InetSocketAddress(host,port):new InetSocketAddress(port);
        if(host==null) host = addr.getHostName();
        this.datagramChannel.bind(addr);
        if(daemon) executorService.execute(this);
    }
    public void shutdown() throws Exception{
        log.warn("UDP endpoint service is going down");
        this.executorService.shutdownNow();
        this.datagramChannel.close();
    }

    @Override
    public void run() {
        log.warn("UDP endpoint service is ready on ["+host+":"+port+"]");
        while (true){
            try{
                DatagramPacket buffer = new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
                this.datagramChannel.receive(buffer);
                pendingMessageQueue.offer(buffer);
            }catch (Exception ex){
                //ignore
                //ex.printStackTrace();
                //try{Thread.sleep(50);}catch (Exception exx){}
            }
        }
    }

    public void send(byte[] data,SocketAddress destination){
        try {
            DatagramPacket packet = new DatagramPacket(data,data.length,destination);
            this.datagramChannel.send(packet);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    @Override
    public void address(String address) {
        this.host = address;
    }

    @Override
    public void backlog(int backlog) {

    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public void inboundThreadPoolSetting(String inboundThreadPoolSetting) {
        this.inboundThreadPoolSetting = inboundThreadPoolSetting;
    }

    @Override
    public void resource(Resource resource) {

    }

    @Override
    public String name() {
        return "UDPEndpointService";
    }
    public void daemon(boolean daemon){
        this.daemon = daemon;
    }

    @Override
    public void registerUserChannel(UserChannel userChannel){
        this.userChannelIndex.put(userChannel.channelId,userChannel);
    }
    
    public void releaseUserChannel(UserChannel userChannel){
        this.userChannelIndex.remove(userChannel.channelId);
    }

}
