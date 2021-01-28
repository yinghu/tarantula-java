package com.icodesoftware.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {
    public static byte[] toJson(Map<String,Object> kv){
        JsonObject json = new JsonObject();
        kv.forEach((k,v)->{
            if(v!=null&& (v instanceof String)){
                json.addProperty(k,(String)v);
            }
            else if(v!=null&& (v instanceof Boolean)){
                json.addProperty(k,(Boolean)v);
            }
            else if(v!=null&& (v instanceof Number)){
                json.addProperty(k,(Number)v);
            }
        });
        return json.toString().getBytes(Charset.forName("UTF-8"));
    }
    public static String toJsonString(Map<String,Object> kv){
        JsonObject json = new JsonObject();
        kv.forEach((k,v)->{
            if(v!=null&& (v instanceof String)){
                json.addProperty(k,(String)v);
            }
            else if(v!=null&& (v instanceof Boolean)){
                json.addProperty(k,(Boolean)v);
            }
            else if(v!=null&& (v instanceof Number)){
                json.addProperty(k,(Number)v);
            }
        });
        return json.toString();//.getBytes(Charset.forName("UTF-8"));
    }
    public static JsonObject toJsonObject(Map<String,Object> kv){
        JsonObject json = new JsonObject();
        kv.forEach((k,v)->{
            if(v!=null&& (v instanceof String)){
                json.addProperty(k,(String)v);
            }
            else if(v!=null&& (v instanceof Boolean)){
                json.addProperty(k,(Boolean)v);
            }
            else if(v!=null&& (v instanceof Number)){
                json.addProperty(k,(Number)v);
            }
        });
        return json;
    }
    public static Map<String,Object> toMap(byte[] json){
        JsonParser jp = new JsonParser();
        InputStreamReader inr = new InputStreamReader(new ByteArrayInputStream(json));
        JsonElement j = jp.parse(inr);
        Map<String,Object> _mv = new HashMap<>();
        j.getAsJsonObject().entrySet().forEach((e)->{
            JsonElement je = e.getValue();
            if(je.isJsonPrimitive()){
                JsonPrimitive m = je.getAsJsonPrimitive();
                if(m.isString()){
                    _mv.put(e.getKey(),m.getAsString());
                }
                else if(m.isNumber()){
                    _mv.put(e.getKey(),m.getAsNumber());
                }
                else if(m.isBoolean()){
                    _mv.put(e.getKey(),m.getAsBoolean());
                }
            }
            else if(!je.isJsonNull()){
                _mv.put(e.getKey(),je);
            }
        });
        return _mv;
    }
}
