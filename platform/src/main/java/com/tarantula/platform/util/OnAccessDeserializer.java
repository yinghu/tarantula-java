package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.OnAccess;
import com.tarantula.platform.OnAccessTrack;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Updated by yinghu on 10/24/2018.
 */
public class OnAccessDeserializer implements JsonDeserializer<OnAccess> {


    public OnAccessDeserializer(){}


    public OnAccess deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        OnAccess   access = new OnAccessTrack();
        JsonObject e = jsonElement.getAsJsonObject();
        e.entrySet().forEach((Map.Entry<String,JsonElement> kv)->{
            String k = kv.getKey();
            JsonPrimitive jo = kv.getValue().getAsJsonPrimitive();
            if(k.equals("systemId")){
                access.systemId(jo.getAsString());
            }
            else if(k.equals("stub")){
                access.stub(jo.getAsInt());
            }
            else if(k.equals("applicationId")){
                access.applicationId(jo.getAsString());
            }
            else if(k.equals("name")){
                access.name(jo.getAsString());
            }
            else if(k.equals("accessMode")){
                access.accessMode(jo.getAsInt());
            }
            else if(k.equals("instanceId")){
                access.instanceId(jo.getAsString());
            }
            else if(k.equals("accessKey")){
                access.accessKey(jo.getAsString());
            }
            else if(k.equals("accessId")){
                access.accessId(jo.getAsString());
            }
            else if(k.equals("typeId")){
                access.typeId(jo.getAsString());
            }
            else if(k.equals("subtypeId")){
                access.subtypeId(jo.getAsString());
            }
            else if(k.equals("oid")){
                access.oid(jo.getAsString());
            }
            else if(k.equals("balance")){
                access.entryCost(jo.getAsDouble());
            }
            else{
                if(!jo.getAsString().trim().equals("")){
                    access.header(k,jo.getAsString());
                }
            }
        });
        return access;
    }
}
