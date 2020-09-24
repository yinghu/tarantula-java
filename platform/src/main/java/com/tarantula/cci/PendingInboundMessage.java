package com.tarantula.cci;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 9/24/2020.
 */
public class PendingInboundMessage {
    public String serverId;
    public ByteBuffer message;
    public PendingInboundMessage(String serverId,ByteBuffer message){
        this.serverId = serverId;
        this.message = message;
    }
}
