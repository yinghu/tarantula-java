package com.tarantula.cci.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;
import com.tarantula.platform.PendingMessage;
import com.tarantula.platform.service.ConnectionEventService;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPSessionService implements ConnectionEventService {

    private DatagramSocket datagramChannel;
    private final Connection serverConnection;
    private final ConcurrentLinkedDeque<PendingMessage> pendingData;
    private final ConcurrentHashMap<Integer,Boolean> pendingAck;

    private final Cipher encrypt;
    private final Cipher decrypt;

    public UDPSessionService(Connection connection, ConcurrentLinkedDeque<PendingMessage> pendingData, Cipher encrypt, Cipher decrypt){
        this.serverConnection = connection;
        this.pendingData = pendingData;
        this.encrypt = encrypt;
        this.decrypt = decrypt;
        pendingAck = new ConcurrentHashMap<>();
    }

    @Override
    public void publish(Event out) {

    }

    @Override
    public void retry(String retryKey) {

    }

    @Override
    public void registerEventListener(String topic, EventListener callback) {

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
        this.datagramChannel.connect(new InetSocketAddress(serverConnection.host(),serverConnection.port()));
    }

    @Override
    public void shutdown() throws Exception {
        this.datagramChannel.close();
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
        int _mid = 1;
        DatagramPacket data = send(payload,seq,ack,_mid,connection);
        if(ack&&data!=null){
            pendingAck.put(_mid,true);
            pendingData.offer(new PendingMessage(new PendingServerPushMessage(this,data,_mid)));
        }
    }
    private DatagramPacket send(byte[] payload,int sequence,boolean ack,int messageId,Connection connection){
        try{
            OutboundMessage pendingOutboundMessage = new OutboundMessage();
            pendingOutboundMessage.ack(ack);
            pendingOutboundMessage.connectionId(connection.channelId());
            pendingOutboundMessage.sessionId(0);
            pendingOutboundMessage.type(MessageHandler.SERVER_PUSH);
            pendingOutboundMessage.sequence(sequence);//client message type
            pendingOutboundMessage.messageId(messageId);
            pendingOutboundMessage.payload(payload);
            byte[] out = serverConnection.secured()?(encrypt(pendingOutboundMessage.message())):(pendingOutboundMessage.message());
            DatagramPacket datagramPacket = new DatagramPacket(out,out.length);
            datagramChannel.send(datagramPacket);
            return datagramPacket;
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    private byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
        synchronized (encrypt){
            return encrypt.doFinal(data);
        }
    }
    private byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        synchronized (decrypt){
            return decrypt.doFinal(data);
        }
    }
    public void ack(byte[] payload){
        try{
            InboundMessage pendingInboundMessage = new InboundMessage("",serverConnection.secured()? ByteBuffer.wrap(decrypt(payload)):ByteBuffer.wrap(payload),null);
            if(pendingInboundMessage.type()== MessageHandler.ACK){
                DataBuffer dataBuffer = new DataBuffer(pendingInboundMessage.payload());
                int sz = dataBuffer.getInt();
                for(int i=0;i<sz;i++){
                    pendingAck.remove(dataBuffer.getInt());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void discharge(int messageId){
        pendingAck.remove(messageId);
    }
    public boolean retry(int messageId){
        return pendingAck.containsKey(messageId);
    }
}
