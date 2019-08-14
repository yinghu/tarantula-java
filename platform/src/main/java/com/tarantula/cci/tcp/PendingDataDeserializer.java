package com.tarantula.cci.tcp;

import com.google.gson.*;
import com.tarantula.Session;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinghu lu on 10/24/2018.
 */
public class PendingDataDeserializer implements JsonDeserializer<PendingData> {

    @Override
    public PendingData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        PendingData pendingData = new PendingData();
        pendingData.headers = new HashMap<>();
        JsonObject e = jsonElement.getAsJsonObject();
        e.entrySet().forEach((Map.Entry<String,JsonElement> kv)->{
            String k = kv.getKey();
            if(k.equals("clientId")){
                pendingData.clientId = kv.getValue().getAsJsonPrimitive().getAsString();
            }
            if(k.equals("serverId")){
                pendingData.serverId = kv.getValue().getAsJsonPrimitive().getAsString();
                pendingData.headers.put("serverId",pendingData.serverId);
            }
            else if(k.equals("streaming")){
                pendingData.streaming = kv.getValue().getAsJsonPrimitive().getAsBoolean();
            }
            else if(k.equals("path")){
                pendingData.path = kv.getValue().getAsJsonPrimitive().getAsString();
            }
            else if(k.equals("token")){
                pendingData.headers.put(Session.TARANTULA_TOKEN,kv.getValue().getAsJsonPrimitive().getAsString());
            }
            else if(k.equals("tag")){
                pendingData.headers.put(Session.TARANTULA_TAG,kv.getValue().getAsJsonPrimitive().getAsString());
            }
            else if(k.equals("action")){
                pendingData.headers.put(Session.TARANTULA_ACTION,kv.getValue().getAsJsonPrimitive().getAsString());
            }
            else if(k.equals("magicKey")){
                pendingData.headers.put(Session.TARANTULA_MAGIC_KEY,kv.getValue().getAsJsonPrimitive().getAsString());
            }
            else if(k.equals("applicationId")){
                pendingData.headers.put(Session.TARANTULA_APPLICATION_ID,kv.getValue().getAsJsonPrimitive().getAsString());
            }
            else if(k.equals("instanceId")){
                pendingData.headers.put(Session.TARANTULA_INSTANCE_ID,kv.getValue().getAsJsonPrimitive().getAsString());
            }
            else if(k.equals("viewId")){
                pendingData.headers.put(Session.TARANTULA_VIEW_ID,kv.getValue().getAsJsonPrimitive().getAsString());
            }
            else if(k.equals("data")){
                pendingData.payload = kv.getValue().getAsJsonObject().toString().getBytes();
            }
            else{
                pendingData.headers.put(k,kv.getValue());
            }
        });
        return pendingData;
    }
}
