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
    private static int BUFFER_SIZE = MessageBuffer.SIZE;
    private static int PORT = 11933;
    private static int MESSAGE_HANDLER_POOL_SIZE = 1;

    private static int SLEEP_TIMEOUT = 5;

    private static int RETRY_TIMEOUT = 200;

    private DatagramSocket datagramChannel;

    private ConcurrentLinkedDeque<DatagramPacket> pendingMessageQueue = new ConcurrentLinkedDeque();

    private ConcurrentHashMap<Integer,UserChannel> userChannelIndex = new ConcurrentHashMap<>();

    private ConcurrentLinkedDeque<PendingOutboundMessage> pendingOutboundMessageQueue = new ConcurrentLinkedDeque();

    private String host;
    private int port = PORT;

    private ExecutorService executorService;
    private String inboundThreadPoolSetting;
    private int messageHandlerSize = MESSAGE_HANDLER_POOL_SIZE;
    private boolean daemon;
    private int sessionTimeout = SESSION_CHECK_INTERVAL;
    private int retryInterval = RETRY_TIMEOUT;
    private int receiverTimeout = 0;
    private PingListener pingListener  = ()->{};

    public void start() throws Exception{
        if(inboundThreadPoolSetting!=null){
            TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool, poolSize, rh)->{
                this.executorService = pool;
                this.messageHandlerSize = poolSize-(daemon?3:2);
            });
        }else{
            executorService = Executors.newFixedThreadPool(MESSAGE_HANDLER_POOL_SIZE+(daemon?3:2),new TarantulaThreadFactory("udp-messaging"));
        }
        log.warn("Inbound message handler number ["+messageHandlerSize+"]");
        log.warn("Ping handler number ["+1+"]");
        log.warn("Outbound message handler number ["+1+"]");
        log.warn("Session Timeout ["+sessionTimeout+"]");
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
                            if(userChannel!=null) userChannel.onMessage(messageHeader,messageBuffer,packet.getSocketAddress());
                        }
                        else{
                            Thread.sleep(SLEEP_TIMEOUT);
                        }
                    }catch (Exception ex){
                        //ignore
                        log.error("unexpected error",ex);
                    }
                }
            });
        }
        executorService.execute(()->{
            long kickoffTimer = sessionTimeout;
            long pingTimer = SESSION_CHECK_INTERVAL;
            while (true){
                try{
                    Thread.sleep(retryInterval);
                    userChannelIndex.forEach((k,v)->v.onRetry());
                    kickoffTimer -= retryInterval;
                    pingTimer -= retryInterval;
                    if(kickoffTimer<=0){
                        userChannelIndex.forEach((k,v)->v.onKickoff());
                        kickoffTimer = sessionTimeout;
                    }
                    if(pingTimer<=0){
                        pingListener.onPing();
                        pingTimer = SESSION_CHECK_INTERVAL;
                    }
                }catch (Exception ex){
                    log.error("unexpected error",ex);
                }
            }
        });
        executorService.execute(()->{
            while (true){
                try{
                    PendingOutboundMessage pendingOutboundMessage = pendingOutboundMessageQueue.poll();
                    if(pendingOutboundMessage!=null){
                        send(pendingOutboundMessage.payload,pendingOutboundMessage.destination);
                    }
                    else{
                        Thread.sleep(SLEEP_TIMEOUT);
                    }
                }catch (Exception ex){
                    log.error("unexpected error",ex);
                }
            }
        });
        this.datagramChannel = new DatagramSocket(null);
        if(receiverTimeout>0) this.datagramChannel.setSoTimeout(receiverTimeout);
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
        log.warn("UDP endpoint service is ready on ["+host+":"+port+"] on ["+(daemon?"daemon thread":"main thread")+"]");
        while (true){
            try{
                DatagramPacket buffer = new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
                this.datagramChannel.receive(buffer);
                pendingMessageQueue.offer(buffer);
            }catch (Exception ex){
                //ignore
                //log.error("unexpected error",ex);
                try{Thread.sleep(SLEEP_TIMEOUT);}catch (Exception exx){}
            }
        }
    }

    public void send(byte[] data,SocketAddress destination){
        try {
            DatagramPacket packet = new DatagramPacket(data,data.length,destination);
            this.datagramChannel.send(packet);
        }catch (Exception ex){
            log.error("unexpected error",ex);
        }
    }
    public void queue(byte[] data,SocketAddress destination){
        pendingOutboundMessageQueue.offer(new PendingOutboundMessage(data,destination));
    }
    @Override
    public void address(String address) {
        this.host = address;
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
    public String name() {
        return "UDPEndpointService";
    }
    public void daemon(boolean daemon){
        this.daemon = daemon;
    }
    public void sessionTimeout(int timeout){
        sessionTimeout = timeout;
    }
    public int sessionTimeout(){return sessionTimeout;}
    public  void retryInterval(int interval){
        retryInterval = interval;
    }
    public void receiverTimeout(int timeout){
        this.receiverTimeout = timeout;
    }
    @Override
    public void registerUserChannel(UserChannel userChannel){
        this.userChannelIndex.put(userChannel.channelId(),userChannel);
    }
    
    public UserChannel releaseUserChannel(int channelId){
        return this.userChannelIndex.remove(channelId);
    }

    public void registerPingListener(PingListener pingListener){
        this.pingListener = pingListener!=null?pingListener:()->{};
    }

}
