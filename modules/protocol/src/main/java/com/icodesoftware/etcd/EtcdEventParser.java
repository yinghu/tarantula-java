package com.icodesoftware.etcd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.Base64Util;
import com.icodesoftware.util.JsonUtil;

import java.util.Base64;

public class EtcdEventParser {

    private EtcdEventParser(){}


    public static void parse(String payload,OnKeyValue onKeyValue){
        JsonObject resp = JsonUtil.parse(payload);
        if(!resp.has("kvs")) return;
        JsonArray kvs = resp.getAsJsonArray("kvs");
        kvs.forEach(e->{
            JsonObject kv = e.getAsJsonObject();
            onKeyValue.on(new String(Base64Util.fromBase64String(kv.get("key").getAsString())),Base64Util.fromBase64String(kv.get("value").getAsString()),kv.get("create_revision").getAsLong());
        });
    }

    public interface OnKeyValue{
        void on(String key,byte[] value,long revision);
    }


    public static void onWatch(String payload,OnEtcEvent<EtcdEvent> onEtcEvent){
        JsonObject result = JsonUtil.parse(payload).get("result").getAsJsonObject();
        if(result.has("created")) return;
        JsonArray events = result.get("events").getAsJsonArray();
        events.forEach(e->{
            boolean deleted = false;
            if(e.getAsJsonObject().has("type") && e.getAsJsonObject().get("type").getAsString().equals("DELETE")){
                deleted = true;
            }
            JsonObject kv = e.getAsJsonObject().get("kv").getAsJsonObject();
            String[] parts = new String(Base64Util.fromBase64String(kv.get("key").getAsString())).split("#");
            EtcdEvent watchEvent = EtcdPortableRegistry.create(Integer.parseInt(parts[2]),parts[1]);
            if(!deleted){
                Recoverable.DataBuffer buffer = BufferProxy.wrap(Base64.getDecoder().decode(kv.get("value").getAsString()));
                watchEvent.read(buffer);
                watchEvent.revision(kv.get("create_revision").getAsLong());
            }
            onEtcEvent.on(watchEvent);
        });
    }

    public interface OnEtcEvent<T extends EtcdEvent>{
        void on(T event);
    }

}
