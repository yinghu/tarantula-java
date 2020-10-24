package com.tarantula.cci.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;
import com.tarantula.platform.service.ConnectionEventService;
import com.tarantula.platform.util.SystemUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 9/21/2020.
 */
public class UDPSessionService implements ConnectionEventService {

    private DatagramChannel datagramChannel;
    private final Connection connection;
    private final ConcurrentLinkedDeque<InboundMessage> pendingData;

    private Thread receiver;
    private final Cipher encrypt;
    private final Cipher decrypt;
    private final AtomicInteger messageId;
    public UDPSessionService(Connection connection,ConcurrentLinkedDeque<InboundMessage> pendingData,Cipher encrypt,Cipher decrypt){
        this.connection = connection;
        this.pendingData = pendingData;
        this.encrypt = encrypt;
        this.decrypt = decrypt;
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
                ByteBuffer buffer = ByteBuffer.allocate(OutboundMessage.MESSAGE_SIZE*2);
                SocketAddress sc = datagramChannel.receive(buffer);
                buffer.flip();
                byte[] payload = new byte[buffer.limit()];
                buffer.get(payload,0,payload.length);
                InboundMessage pendingInboundMessage = new InboundMessage(connection.serverId(),connection.secured()?ByteBuffer.wrap(decrypt(payload)):ByteBuffer.wrap(payload),sc);
                if(pendingInboundMessage.ack()){
                    continue;
                }
                pendingData.offer(pendingInboundMessage);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void publish(byte[] payload,String label,Connection connection) {
        String[] params = label.split(Recoverable.PATH_SEPARATOR);
        int type = Integer.parseInt(params[0]);
        int seq = Integer.parseInt(params[1]);
        boolean ack = params.length==3?Boolean.parseBoolean(params[2]):false;
        send(payload,type,seq,ack,connection);
    }
    private void send(byte[] payload,int type,int sequence,boolean ack,Connection connection){
        try{
            OutboundMessage pendingOutboundMessage = new OutboundMessage();
            pendingOutboundMessage.ack(ack);
            pendingOutboundMessage.connectionId(connection.connectionId());
            pendingOutboundMessage.sessionId(0);
            pendingOutboundMessage.type(type);
            pendingOutboundMessage.sequence(sequence);
            pendingOutboundMessage.messageId(messageId.incrementAndGet());
            pendingOutboundMessage.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
            pendingOutboundMessage.payload(payload);
            ByteBuffer out = connection.secured()?ByteBuffer.wrap(encrypt(pendingOutboundMessage.message())):ByteBuffer.wrap(pendingOutboundMessage.message());
            datagramChannel.write(out);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
        return encrypt.doFinal(data);
    }
    private byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        return decrypt.doFinal(data);
    }
}
