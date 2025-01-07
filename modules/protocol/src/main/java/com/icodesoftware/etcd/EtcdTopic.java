package com.icodesoftware.etcd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class EtcdTopic extends RecoverableObject {

    public String topic;
    public final ConcurrentHashMap<String,EtcdSubscribe> subscribers = new ConcurrentHashMap<>();

    private EtcdTopic(String topic){
        this.topic = topic;
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
    }

    public static EtcdTopic create(String topic){
        return new EtcdTopic(topic);
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("topic",topic);
        resp.addProperty("startTime",TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        JsonArray subs = new JsonArray();
        subscribers.forEach((k,v)->{
            subs.add(v.toJson());
        });
        resp.add("subscribers",subs);
        return resp;
    }
}
