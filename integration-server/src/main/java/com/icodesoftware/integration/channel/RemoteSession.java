package com.icodesoftware.integration.channel;

import com.icodesoftware.integration.GameSession;
import com.icodesoftware.util.FIFOBuffer;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 10/18/2020.
 */
public class RemoteSession implements GameSession {
    public final SocketAddress socketAddress;
    public final FIFOBuffer<Integer> ackBuffer;
    public final AtomicInteger pingPong;
    public final int[] messageRange;
    public final int seat;

    public RemoteSession(final int seat,final int[] messageRange,final  SocketAddress socketAddress,final FIFOBuffer<Integer> ackBuffer){
        this.seat = seat;
        this.socketAddress = socketAddress;
        this.ackBuffer = ackBuffer;
        this.pingPong = new AtomicInteger(0);
        this.messageRange = new int[]{messageRange[0],messageRange[1]};
    }
    public boolean validate(SocketAddress socketAddress){
        return this.socketAddress.equals(socketAddress);
    }
    public int seat(){
        return seat;
    }
}
