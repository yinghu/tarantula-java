package com.icodesoftware.etcd;

public class EtcdPortableRegistry{

    public static final int WATCH_EVENT_CID = 1;
    public static final int TOPIC_EVENT_CID = 2;
    public static final int SUBSCRIBE_EVENT_CID = 3;
    public static final int CLAIM_EVENT_CID = 4;


    public static <T extends EtcdEvent> T create(int cid,String key) {
        EtcdEvent _ins;
        switch(cid){
            case WATCH_EVENT_CID:
                _ins = WatchEvent.fromKey(key);
                break;
            case TOPIC_EVENT_CID:
                _ins = TopicEvent.create();
                break;
            case SUBSCRIBE_EVENT_CID:
                _ins = SubscribeEvent.create();
                break;
            case CLAIM_EVENT_CID:
                _ins = ClaimEvent.create();
                break;
            default:
                throw new RuntimeException("Class ID ["+cid+"] not supported");
        }
        return (T)_ins;
    }
}
