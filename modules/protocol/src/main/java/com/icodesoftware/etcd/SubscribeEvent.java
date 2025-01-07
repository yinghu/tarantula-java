package com.icodesoftware.etcd;

public class SubscribeEvent extends EtcdEvent{

    public static final String SUBSCRIBE = "subscribe";

    public String topic;

    private SubscribeEvent(){
        this.key = SUBSCRIBE;

    }
    private SubscribeEvent(String topic, String nodeName){
        this();
        this.topic = topic;
        this.nodeName = nodeName;
    }

    @Override
    public int getClassId() {
        return EtcdPortableRegistry.SUBSCRIBE_EVENT_CID;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        topic = buffer.readUTF8();
        nodeName = buffer.readUTF8();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(topic);
        buffer.writeUTF8(nodeName);
        return true;
    }

    public static SubscribeEvent create(){
        return new SubscribeEvent();
    }

    public static SubscribeEvent create(String topic, String nodeName){
        return new SubscribeEvent(topic,nodeName);
    }
}
