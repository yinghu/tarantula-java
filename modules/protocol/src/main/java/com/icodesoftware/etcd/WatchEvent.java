package com.icodesoftware.etcd;

import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class WatchEvent extends EtcdEvent {

    public static final String JOIN = "join";
    public static final String PING = "ping";

    @Override
    public int getClassId() {
        return EtcdPortableRegistry.WATCH_EVENT_CID;
    }

    private WatchEvent(String key, String value){
        this(key);
        this.nodeName = value;
    }

    private WatchEvent(String key){
        this.key = key;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        nodeName = buffer.readUTF8();
        timestamp = buffer.readLong();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(nodeName);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        return true;
    }

    public static WatchEvent join(String node){
        return new WatchEvent(JOIN,node);
    }

    public static WatchEvent ping(String node){
        return new WatchEvent(PING,node);
    }

    public static WatchEvent fromKey(String key){
        return new WatchEvent(key);
    }

    @Override
    public String toString() {
        return key().toString()+" : "+nodeName+" : "+TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+" : "+revision;
    }
}
