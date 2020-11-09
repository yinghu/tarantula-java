package com.tarantula.cci.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;
import com.tarantula.platform.service.ConnectionEventService;
import com.tarantula.platform.util.SystemUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 9/21/2020.
 */
public class UDPSessionService implements ConnectionEventService {

    private DatagramSocket datagramChannel;
    private final Connection connection;
    private final ConcurrentLinkedDeque<PendingServerPushMessage> pendingData;

    private final Cipher encrypt;
    private final Cipher decrypt;
    private final AtomicInteger messageId;
    public UDPSessionService(Connection connection,ConcurrentLinkedDeque<PendingServerPushMessage> pendingData,Cipher encrypt,Cipher decrypt){
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
        this.datagramChannel = new DatagramSocket();
        this.datagramChannel.connect(new InetSocketAddress(connection.host(),connection.port()));
    }

    @Override
    public void shutdown() throws Exception {
        this.datagramChannel.close();
    }
    public boolean secured(){
        return this.connection.secured();
    }
    public void receive(DatagramPacket datagramPacket) throws Exception{
        datagramChannel.setSoTimeout(500);
        datagramChannel.receive(datagramPacket);
    }
    public void send(DatagramPacket datagramPacket) throws Exception{
        datagramChannel.send(datagramPacket);
    }

    @Override
    public void publish(byte[] payload,String label,Connection connection) {
        String[] params = label.split(Recoverable.PATH_SEPARATOR);
        int seq = Integer.parseInt(params[0]);
        boolean ack = params.length==2?Boolean.parseBoolean(params[1]):false;
        int _mid = messageId.incrementAndGet();
        DatagramPacket data = send(payload,seq,ack,_mid,connection);
        if(ack&&data!=null){
            pendingData.offer(new PendingServerPushMessage(this,data,_mid));
        }
    }
    private DatagramPacket send(byte[] payload,int sequence,boolean ack,int messageId,Connection connection){
        try{
            OutboundMessage pendingOutboundMessage = new OutboundMessage();
            pendingOutboundMessage.ack(ack);
            pendingOutboundMessage.connectionId(connection.connectionId());
            pendingOutboundMessage.sessionId(0);
            pendingOutboundMessage.type(MessageHandler.SERVER_PUSH);
            pendingOutboundMessage.sequence(sequence);
            pendingOutboundMessage.messageId(messageId);
            pendingOutboundMessage.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
            pendingOutboundMessage.payload(payload);
            byte[] out = connection.secured()?(encrypt(pendingOutboundMessage.message())):(pendingOutboundMessage.message());
            DatagramPacket datagramPacket = new DatagramPacket(out,out.length);
            datagramChannel.send(datagramPacket);
            return datagramPacket;
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    private byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
        return encrypt.doFinal(data);
    }
    public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        return decrypt.doFinal(data);
    }
}
