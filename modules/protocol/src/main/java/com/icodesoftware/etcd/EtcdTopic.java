package com.icodesoftware.etcd;

import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class EtcdTopic extends EtcdEvent{

    public static final String TOPIC = "topic";

    private String topic;

    private EtcdTopic(){
        this.key = TOPIC;
    }
    private EtcdTopic(String topic){
        this();
        this.topic = topic;
    }
    @Override
    public int getClassId() {
        return EtcdPortableRegistry.TOPIC_EVENT_CID;
    }
    @Override
    public boolean read(DataBuffer buffer) {
        topic = buffer.readUTF8();
        timestamp = buffer.readLong();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(topic);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        return true;
    }

    public static EtcdTopic create(){
        return new EtcdTopic();
    }

    public static EtcdTopic create(String topic){
        return new EtcdTopic(topic);
    }
}
