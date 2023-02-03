package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.OnAccess;
import com.tarantula.platform.OnAccessTrack;

import java.lang.reflect.Type;
import java.util.Map;

public class OnAccessDeserializer implements JsonDeserializer<OnAccess> {


    public OnAccessDeserializer(){}

    //format {{k,v},{k,v},[{k,v},[k,v]]}
    public OnAccess deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        OnAccess   access = new OnAccessTrack();
        JsonObject e = jsonElement.getAsJsonObject();
        e.entrySet().forEach((Map.Entry<String,JsonElement> kv)->{
            String k = toLowercaseAtFirst(kv.getKey());
            JsonElement ve = kv.getValue();
            if(ve.isJsonPrimitive()){
                JsonPrimitive jo = ve.getAsJsonPrimitive();
                if(jo.isString()){
                    access.property(k,jo.getAsString());
                    _setProperty(access,k,jo.getAsString());
                }
                if(jo.isNumber()){
                    access.property(k,jo.getAsNumber());
                    _setProperty(access,k,jo.getAsNumber());
                }
                if(jo.isBoolean()){
                    access.property(k,jo.getAsBoolean());
                    _setProperty(access,k,jo.getAsBoolean());
                }
            }
            else if(ve.isJsonArray()){
                JsonArray alist = ve.getAsJsonArray();
                alist.forEach((a)->{//key value pair
                    JsonObject nv = a.getAsJsonObject();
                    String _k;
                    JsonPrimitive jp;
                    if(nv.has("Name")){
                        _k = toLowercaseAtFirst(nv.get("Name").getAsString());
                        jp = nv.get("Value").getAsJsonPrimitive();
                    }
                    else{
                        _k = nv.get("name").getAsString();
                        jp = nv.get("value").getAsJsonPrimitive();
                    }
                    if(jp.isString()){
                        access.property(_k,jp.getAsString());
                        _setProperty(access,_k,jp.getAsString());
                    }
                    if(jp.isNumber()){
                        access.property(_k,jp.getAsNumber());
                        _setProperty(access,_k,jp.getAsNumber());
                    }
                    if(jp.isBoolean()){
                        access.property(_k,jp.getAsBoolean());
                        _setProperty(access,_k,jp.getAsBoolean());
                    }
                });
            }
        });
        return access;
    }
    private void _setProperty(OnAccess access,String k,Object v){
        if(k.equals("systemId")){
            access.systemId((String) v);
        }
        else if(k.equals("stub")){
            access.stub(((Number)v).intValue());
        }
        else if(k.equals("name")){
            access.name((String) v);
        }
        //else if(k.equals("accessMode")){
            //access.accessMode(((Number)v).intValue());
        //}
        else if(k.equals("tournamentId")){
            access.tournamentId((String) v);
        }
        else if(k.equals("typeId")){
            access.typeId((String) v);
        }
        else if(k.equals("oid")){
            access.oid((String) v);
        }
        else if(k.equals("balance")){
            access.balance(((Number)v).doubleValue());
        }
        else if(k.equals("timestamp")){
            access.timestamp(((Number)v).longValue());
        }
        else{
            access.property(k,v);
        }
    }
    private String toLowercaseAtFirst(String str){
        char[] chars = str.toCharArray();
        char first =Character.toLowerCase(chars[0]);
        chars[0]=first;
        return new String(chars);
    }
}
