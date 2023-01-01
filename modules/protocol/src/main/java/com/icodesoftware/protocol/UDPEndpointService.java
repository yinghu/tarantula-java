package com.icodesoftware.protocol;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.icodesoftware.util.TarantulaThreadFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final public class UDPEndpointService implements UDPEndpointServiceProvider {

    private static TarantulaLogger log = JDKLogger.getLogger(UDPEndpointService.class);
    private static int BUFFER_SIZE = MessageBuffer.SIZE;
    private static int PORT = 11933;
    private static int MESSAGE_HANDLER_POOL_SIZE = 1;


    private DatagramSocket datagramChannel;

    private ConcurrentLinkedDeque<DatagramPacket> pendingInboundMessageQueue = new ConcurrentLinkedDeque();
    private ConcurrentLinkedDeque<byte[]> pendingBufferQueue = new ConcurrentLinkedDeque();


    private ConcurrentHashMap<Integer,UserChannel> userChannelIndex = new ConcurrentHashMap<>();

    private ConcurrentLinkedDeque<PendingOutboundMessage> pendingOutboundMessageQueue = new ConcurrentLinkedDeque();

    private String host;
    private int port = PORT;

    private ExecutorService executorService;
    private String inboundThreadPoolSetting;
    private int messageHandlerSize = MESSAGE_HANDLER_POOL_SIZE;

    //timer counters
    private int sessionTimeout = SESSION_CHECK_INTERVAL;
    private int retryInterval = RETRY_TIMEOUT;
    private int receiverTimeout = 0;
    private int frameRate = PENDING_ACTION_INTERVAL;

    private PingListener pingListener  = ()->{};

    private boolean running = true;

    private long kickoffTimer;// = sessionTimeout;
    private long pingTimer = SESSION_CHECK_INTERVAL;
    private long serverPingTimer  = SERVER_PING_INTERVAL;
    private long retryTimer;// = retryInterval;

    private UDPOperationSummary operationSummary = new UDPOperationSummary();
    public void start() throws Exception{
        if(inboundThreadPoolSetting!=null){
            TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool, poolSize, rh)->{
                this.executorService = pool;
                this.messageHandlerSize = poolSize;
            });
        }else{
            executorService = Executors.newFixedThreadPool(MESSAGE_HANDLER_POOL_SIZE,new TarantulaThreadFactory("udp-messaging"));
        }
        log.warn("Inbound message handler number ["+messageHandlerSize+"]");
        log.warn("Ping handler number ["+1+"]");
        log.warn("Outbound message handler number ["+1+"]");
        log.warn("Session Timeout ["+sessionTimeout+"]");
        log.warn("Receiver Timeout ["+receiverTimeout+"]");
        kickoffTimer = sessionTimeout;
        retryTimer = retryInterval;
        for(int i=0;i<messageHandlerSize;i++){
            executorService.execute(()->{
                MessageBuffer messageBuffer = new MessageBuffer();
                while(running){
                    try{
                        DatagramPacket packet = pendingInboundMessageQueue.poll();
                        if(packet!=null){
                            operationSummary.pendingInboundMessageNumber.decrementAndGet();
                            messageBuffer.reset(packet.getData(),0,packet.getLength());
                            messageBuffer.flip();
                            MessageBuffer.MessageHeader messageHeader = messageBuffer.readHeader();
                            UserChannel userChannel = userChannelIndex.get(messageHeader.channelId);
                            if(userChannel!=null){
                                userChannel.onMessage(messageHeader,messageBuffer,packet.getSocketAddress());
                            }
                            pendingBufferQueue.offer(packet.getData());
                        }
                        else{
                            Thread.sleep(SLEEP_TIME_OUT);
                        }
                    }catch (Exception ex){
                        //ignore
                        log.error("unexpected error",ex);
                    }
                }
            });
        }
        this.datagramChannel = new DatagramSocket(null);
        if(receiverTimeout>0) this.datagramChannel.setSoTimeout(receiverTimeout);
        InetSocketAddress addr = host!=null?new InetSocketAddress(host,port):new InetSocketAddress(port);
        if(host==null) host = addr.getHostName();
        this.datagramChannel.bind(addr);
    }
    public void shutdown() throws Exception{
        log.warn("UDP endpoint service is going down");
        running = false;
        this.executorService.shutdownNow();
        this.datagramChannel.close();
    }
    public void onTimer(){
        try{
            userChannelIndex.forEach((k,v)->v.onPendingAction(frameRate));//enqueue pending action data
            retryTimer -= frameRate;
            kickoffTimer -= frameRate;
            pingTimer -= frameRate;
            serverPingTimer -= frameRate;
            if(retryTimer<=0){
                userChannelIndex.forEach((k,v)->v.onRetry());//enqueue retry data
                retryTimer = retryInterval;
            }
            if(kickoffTimer<=0){
                userChannelIndex.forEach((k,v)->v.onKickoff());//remove session
                kickoffTimer = sessionTimeout;
            }
            if(pingTimer<=0){
                pingListener.onPing(); //ping game cluster server
                pingTimer = SESSION_CHECK_INTERVAL;
            }
            if(serverPingTimer<=0){
                userChannelIndex.forEach((k,v)->v.onPing());//ping client from udp server
                serverPingTimer = SERVER_PING_INTERVAL;
            }
        }catch (Exception ex){
            log.error("unexpected error on timer",ex);
        }
    }
    public boolean onOutboundMessage(){
        PendingOutboundMessage pendingOutboundMessage = pendingOutboundMessageQueue.poll();
        if(pendingOutboundMessage==null) return false;
        operationSummary.pendingOutboundMessageNumber.decrementAndGet();
        wire(pendingOutboundMessage.buffer,pendingOutboundMessage.length,pendingOutboundMessage.destination);
        if(pendingOutboundMessage.buffering){
            pendingBufferQueue.offer(pendingOutboundMessage.buffer);
        }
        return true;
    }
    public boolean onReceiveMessage(){
        byte[] buffer = this.buffer();
        try{
            DatagramPacket packet = new DatagramPacket(buffer,BUFFER_SIZE);
            this.datagramChannel.receive(packet);
            pendingInboundMessageQueue.offer(packet);
            operationSummary.pendingInboundMessageNumber.incrementAndGet();
            return true;
        }catch (Exception ex){
            //ignore
            buffer(buffer);
            return false;
        }
    }

    public void queue(byte[] data,int length,SocketAddress destination){
        pendingOutboundMessageQueue.offer(new PendingOutboundMessage(data,length,destination,false));
        operationSummary.pendingOutboundMessageNumber.incrementAndGet();
    }

    public void queue(MessageBuffer messageBuffer,SocketAddress destination){
        byte[] buffer = this.buffer();
        int len = messageBuffer.toArray(buffer);
        pendingOutboundMessageQueue.offer(new PendingOutboundMessage(buffer,len,destination));
        operationSummary.pendingOutboundMessageNumber.incrementAndGet();
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
        this.operationSummary.userChannelNumber.incrementAndGet();
    }
    
    public UserChannel releaseUserChannel(int channelId){
        this.operationSummary.userChannelNumber.decrementAndGet();
        return this.userChannelIndex.remove(channelId);
    }

    public void registerPingListener(PingListener pingListener){
        if(pingListener==null) return;
        this.pingListener = pingListener;
    }

    @Override
    public void registerSummary(Summary summary){
        summary.registerCategory(UDPOperationSummary.USER_CHANNEL_NUMBER);
        summary.registerCategory(UDPOperationSummary.PENDING_INBOUND_MESSAGE_NUMBER);
        summary.registerCategory(UDPOperationSummary.PENDING_OUTBOUND_MESSAGE_NUMBER);
        summary.registerCategory(UDPOperationSummary.PENDING_BUFFER_NUMBER);
    }
    @Override
    public void updateSummary(Summary summary){
        summary.update(UDPOperationSummary.USER_CHANNEL_NUMBER,operationSummary.userChannelNumber.get());
        summary.update(UDPOperationSummary.PENDING_INBOUND_MESSAGE_NUMBER,operationSummary.pendingInboundMessageNumber.get());
        summary.update(UDPOperationSummary.PENDING_OUTBOUND_MESSAGE_NUMBER,operationSummary.pendingOutboundMessageNumber.get());
        summary.update(UDPOperationSummary.PENDING_BUFFER_NUMBER,operationSummary.pendingBufferNumber.get());
    }
    public byte[] buffer(){
        byte[] buffer = pendingBufferQueue.poll();
        if(buffer==null){
            buffer = new byte[BUFFER_SIZE];
            operationSummary.pendingBufferNumber.incrementAndGet();
        }
        return buffer;
    }
    public void buffer(byte[] buffer){
        pendingBufferQueue.offer(buffer);
    }
    private void wire(byte[] buffer,int length,SocketAddress destination){
        DatagramPacket packet = new DatagramPacket(buffer,length,destination);
        try{
            this.datagramChannel.send(packet);
        }catch (Exception ex){
            log.error("unexpected error on send",ex);
        }
    }
}
