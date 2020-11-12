package com.tarantula.cci.udp;
import com.icodesoftware.protocol.OutboundMessage;

import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
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
    public void ack(){
        try{
            DatagramPacket datagramPacket = new DatagramPacket(new byte[OutboundMessage.MESSAGE_SIZE*2],OutboundMessage.MESSAGE_SIZE*2);
            udpSessionService.receive(datagramPacket);
            byte[] payload = Arrays.copyOf(datagramPacket.getData(),datagramPacket.getLength());
            udpSessionService.ack(payload);
        }
        catch (Exception ex){
            if(!(ex instanceof SocketTimeoutException)){
                ex.printStackTrace();
            }
        }
    }
    public boolean retry(){
        if(!udpSessionService.retry(messageId)){
            return false;
        }
        retries--;
        if(retries<=0){
            udpSessionService.discharge(messageId);
            return false;
        }
        try{
            udpSessionService.send(data);
            return true;
        }catch (Exception ex){
            return false;
        }
    }
}
