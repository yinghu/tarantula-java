package com.icodesoftware.etcd;

import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class ClaimEvent extends EtcdEvent{

    public static final String CLAIM = "claim";

    public String httpEndpoint;

    private ClaimEvent(){
        this.key = CLAIM;
    }
    private ClaimEvent(String nodeName, String httpEndpoint){
        this();
        this.nodeName = nodeName;
        this.httpEndpoint = httpEndpoint;
    }
    @Override
    public int getClassId() {
        return EtcdPortableRegistry.CLAIM_EVENT_CID;
    }
    @Override
    public boolean read(DataBuffer buffer) {
        nodeName = buffer.readUTF8();
        httpEndpoint = buffer.readUTF8();
        timestamp = buffer.readLong();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(nodeName);
        buffer.writeUTF8(httpEndpoint);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        return true;
    }

    public static ClaimEvent create(){
        return new ClaimEvent();
    }

    public static ClaimEvent create(String nodeName, String httpEndpoint){
        return new ClaimEvent(nodeName,httpEndpoint);
    }
}
