package com.tarantula.cci.tcp;

import com.tarantula.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinghu lu on 10/24/2018.
 */
public class PendingData {
    public String clientId;
    public String path;
    public Map<String,Object> headers;
    public byte[] payload;
    public boolean streaming;
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
