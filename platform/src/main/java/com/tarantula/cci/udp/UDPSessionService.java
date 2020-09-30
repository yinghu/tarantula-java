package com.tarantula.cci.udp;


import com.icodesoftware.Recoverable;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;
import com.tarantula.*;
import com.tarantula.platform.service.ConnectionEventService;

import javax.crypto.Cipher;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 9/21/2020.
 */
public class UDPSessionService implements ConnectionEventService {

    private DatagramChannel datagramChannel;
    private final Connection connection;
    private final ConcurrentLinkedDeque<PendingInboundMessage> pendingData;

    private Thread receiver;
    private final Cipher encrypt;
    private final AtomicInteger messageId;
    public UDPSessionService(Connection connection,ConcurrentLinkedDeque<PendingInboundMessage> pendingData,Cipher cipher){
        this.connection = connection;
        this.pendingData = pendingData;
        this.encrypt = cipher;
        messageId = new AtomicInteger(0);
    }

    @Override
    public void publish(Event out) {

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
                SocketAddress sc = datagramChannel.receive(buffer);
                PendingInboundMessage pendingInboundMessage = new PendingInboundMessage(connection.serverId(),buffer,sc);
                if(pendingInboundMessage.ack()){
                    continue;
                }
                pendingData.offer(pendingInboundMessage);
            }
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    @Override
    public void publish(byte[] payload,String label,Connection connection) {
        try{
            PendingOutboundMessage pendingOutboundMessage = new PendingOutboundMessage();
            pendingOutboundMessage.ack(false);
            pendingOutboundMessage.connectionId(connection.connectionId());
            //label sequence/type
            String[] params = label.split(Recoverable.PATH_SEPARATOR);
            pendingOutboundMessage.type(Integer.parseInt(params[1]));
            ByteBuffer seq = ByteBuffer.allocate(32);
            seq.putInt(Integer.parseInt(params[0]));
            pendingOutboundMessage.sequence(encrypt.doFinal(seq.array()));
            pendingOutboundMessage.messageId(messageId.incrementAndGet());
            pendingOutboundMessage.payload(payload);
            datagramChannel.write(pendingOutboundMessage.message());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
