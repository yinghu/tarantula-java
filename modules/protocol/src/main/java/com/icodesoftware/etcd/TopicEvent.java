package com.icodesoftware.etcd;

import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class TopicEvent extends EtcdEvent{

    public static final String TOPIC = "topic";

    public String topic;

    private TopicEvent(){
        this.key = TOPIC;
    }
    private TopicEvent(String topic){
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

    public static TopicEvent create(){
        return new TopicEvent();
    }

    public static TopicEvent create(String topic){
        return new TopicEvent(topic);
    }
}
