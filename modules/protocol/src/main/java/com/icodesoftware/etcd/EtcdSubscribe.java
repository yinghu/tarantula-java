package com.icodesoftware.etcd;

public class EtcdSubscribe extends EtcdEvent{

    public static final String SUBSCRIBE = "subscribe";

    public String topic;

    private EtcdSubscribe(){
        this.key = SUBSCRIBE;

    }
    private EtcdSubscribe(String topic,String nodeName){
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

    public static EtcdSubscribe create(){
        return new EtcdSubscribe();
    }

    public static EtcdSubscribe create(String topic,String nodeName){
        return new EtcdSubscribe(topic,nodeName);
    }
}
