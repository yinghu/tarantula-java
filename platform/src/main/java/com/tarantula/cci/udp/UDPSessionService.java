package com.tarantula.cci.udp;


import com.tarantula.*;
import com.tarantula.cci.PendingInboundMessage;
import com.tarantula.cci.PendingOutboundMessage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by yinghu lu on 9/21/2020.
 */
public class UDPSessionService implements EventService{

    private DatagramChannel datagramChannel;
    private final Connection connection;
    private final ConcurrentLinkedDeque<PendingInboundMessage> pendingData;

    private Thread receiver;
    private final Cipher cipher;
    private final SecretKey secretKey;
    public UDPSessionService(Connection connection,ConcurrentLinkedDeque<PendingInboundMessage> pendingData,Cipher cipher,SecretKey secretKey){
        this.connection = connection;
        this.pendingData = pendingData;
        this.cipher = cipher;
        this.secretKey = secretKey;
    }

    @Override
    public void publish(Event out) {
        try{
            PendingOutboundMessage pendingOutboundMessage = new PendingOutboundMessage();
            pendingOutboundMessage.ack(false);
            pendingOutboundMessage.type(out.code());
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            ByteBuffer seq = ByteBuffer.allocate(32);
            seq.putInt(out.stub());
            pendingOutboundMessage.sequence(cipher.doFinal(seq.array()));
            pendingOutboundMessage.payload(out.payload());
            datagramChannel.write(pendingOutboundMessage.message());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public String subscription() {
        return null;
    }

    @Override
    public void retry(String retryKey) {

    }

    @Override
    public void registerEventListener(String topic, EventListener callback) {

    }

    @Override
    public RoutingKey instanceRoutingKey(String applicationId, String instanceId) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag, int routingNumber) {
        return null;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void start() throws Exception {
        this.datagramChannel = DatagramChannel.open();
        this.datagramChannel.connect(new InetSocketAddress(connection.host(),connection.port()));
        this.receiver = new Thread(()->{
           run();
        });
        this.receiver.setName("tarantula-udp-"+connection.serverId());
        this.receiver.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.datagramChannel.close();
        this.receiver.interrupt();
    }

    private void run() {
        try{
            while (true){
                ByteBuffer buffer = ByteBuffer.allocate(PendingOutboundMessage.MESSAGE_SIZE);
                datagramChannel.receive(buffer);
                PendingInboundMessage pendingInboundMessage = new PendingInboundMessage(connection.serverId(),buffer);
                if(pendingInboundMessage.ack()){
                    continue;
                }
                pendingData.offer(pendingInboundMessage);
            }
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }
}
