package com.tarantula.game;

import com.icodesoftware.protocol.DataBuffer;

/**
 * Created by yinghu lu on 11/14/2020.
 */
public class PendingUpdate {
    public String label;
    public DataBuffer pending;

    public PendingUpdate(String label,DataBuffer dataBuffer){
        this.label = label;
        this.pending = dataBuffer;
    }
}
