package com.tarantula.game;

import com.icodesoftware.protocol.DataBuffer;

public class PendingUpdate {
    public String label;
    public DataBuffer pending;

    public PendingUpdate(String label,DataBuffer dataBuffer){
        this.label = label;
        this.pending = dataBuffer;
    }
}
