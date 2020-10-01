package com.tarantula.cci.tcp;

import com.icodesoftware.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * Updated by yinghu lu on 12/16/2019.
 */
public class PendingData {
    public String clientId;
    public String path;
    public Map<String,Object> headers;
    public byte[] payload;
    public boolean streaming;
    public boolean oneWay;
    public String serverId;
    public PendingData(){}
    public PendingData(String path,String tag,String action,byte[] payload){
        this.path = path;
        this.headers = new HashMap<>();
        this.headers.put(Session.TARANTULA_TAG,tag);
        this.headers.put(Session.TARANTULA_ACTION,action);
        this.payload = payload;
        this.streaming = false;
    }
}
