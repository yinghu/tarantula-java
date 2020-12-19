package com.tarantula.platform;

import com.tarantula.cci.udp.PendingServerPushMessage;

/**
 * Created by yinghu lu on 12/19/2020.
 */
public class PendingMessage {


    public PendingServerPushMessage pendingServerPushMessage;
    public Runnable runnable;
    public boolean outbound;

    public PendingMessage(PendingServerPushMessage pendingServerPushMessage){
        this.pendingServerPushMessage = pendingServerPushMessage;
        this.outbound = true;
    }
    public PendingMessage(Runnable inboundMessage){
        this.runnable = inboundMessage;
        this.outbound = false;
    }

}
