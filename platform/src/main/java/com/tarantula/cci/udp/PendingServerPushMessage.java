package com.tarantula.cci.udp;

import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Created by yinghu lu on 11/8/2020.
 */
public class PendingServerPushMessage {

    private final UDPSessionService udpSessionService;
    private final DatagramPacket data;
    private final int messageId;
    private int retries = 3;
    public PendingServerPushMessage(final UDPSessionService udpSessionService,final DatagramPacket data,final int messageId){
        this.udpSessionService = udpSessionService;
        this.data = data;
        this.messageId = messageId;
    }
    public boolean ack(){
        boolean acked = false;
        try{
            DatagramPacket datagramPacket = new DatagramPacket(new byte[OutboundMessage.MESSAGE_SIZE*2],OutboundMessage.MESSAGE_SIZE*2);
            udpSessionService.receive(datagramPacket);
            byte[] payload = Arrays.copyOf(datagramPacket.getData(),datagramPacket.getLength());
            InboundMessage pendingInboundMessage = new InboundMessage("",udpSessionService.secured()?ByteBuffer.wrap(udpSessionService.decrypt(payload)):ByteBuffer.wrap(payload),null);
            if(pendingInboundMessage.type()== MessageHandler.ACK){
                DataBuffer dataBuffer = new DataBuffer(pendingInboundMessage.payload());
                int sz = dataBuffer.getInt();
                for(int i=0;i<sz;i++){
                    if(messageId==dataBuffer.getInt()){
                        acked = true;
                        break;
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return acked;
    }
    public boolean retry(){
        retries--;
        if(retries<0){
            return false;
        }
        try{ udpSessionService.send(data); }catch (Exception e){e.printStackTrace();}
        return true;
    }
}
