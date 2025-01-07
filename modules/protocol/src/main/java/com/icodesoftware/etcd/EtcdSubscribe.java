package com.icodesoftware.etcd;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EtcdSubscribe extends RecoverableObject {


    public String topic;
    public String nodeName;

    private EtcdSubscribe(String topic,String nodeName){
        this.topic = topic;
        this.nodeName = nodeName;
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("topic",topic);
        resp.addProperty("nodeName",nodeName);
        resp.addProperty("startTime", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return resp;
    }
    public static EtcdSubscribe create(String topic,String nodeName){
        return new EtcdSubscribe(topic,nodeName);
    }
}
