package com.icodesoftware.util;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {
    public static byte[] toJson(Map<String,Object> kv){
        return toJsonObject(kv).toString().getBytes(Charset.forName("UTF-8"));
    }
    public static String toJsonString(Map<String,Object> kv){
        return toJsonObject(kv).toString();
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
            else if(v!=null && v instanceof JsonElement){
                json.add(k,(JsonElement)v);
            }
        });
        return json;
    }
    public static Map<String,Object> toMap(byte[] json){
        return toMap(new ByteArrayInputStream(json));

    }
    public static String toSimpleResponse(boolean successful,String message){
        JsonObject resp = new JsonObject();
        resp.addProperty("successful",successful);
        resp.addProperty("message",message);
        resp.addProperty("Successful",successful);
        resp.addProperty("Message",message);
        return resp.toString();
    }
    public synchronized static JsonElement parseAsJsonElement(byte[] json){
        return JsonParser.parseReader(new InputStreamReader(new ByteArrayInputStream(json)));
    }
    public synchronized static JsonArray parseAsJsonArray(String json){
        return JsonParser.parseString(json).getAsJsonArray();
    }
    public synchronized static JsonObject parse(String json){
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public static JsonObject parse(byte[] json){
        return parse(new ByteArrayInputStream(json));
    }
    public synchronized static JsonObject parse(InputStream jsonInput){
        InputStreamReader inr = new InputStreamReader(jsonInput);
        return JsonParser.parseReader(inr).getAsJsonObject();
    }
    public synchronized static Map<String,Object> toMap(InputStream jsonInput){
        InputStreamReader inr = new InputStreamReader(jsonInput);
        JsonElement j = JsonParser.parseReader(inr);
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

    public static int getJsonInt(JsonObject obj, String key, int defaultVal)
    {
        if(obj.has(key)) return obj.get(key).getAsInt();
        return defaultVal;
    }

    public static long getJsonLong(JsonObject obj, String key, long defaultVal)
    {
        if(obj.has(key)) return obj.get(key).getAsLong();
        return defaultVal;
    }

    public static String getJsonString(JsonObject obj, String key, String defaultVal)
    {
        if(obj.has(key)) return obj.get(key).getAsString();
        return defaultVal;
    }

    public static boolean getJsonBool(JsonObject obj, String key, boolean defaultVal)
    {
        if(obj.has(key)) return obj.get(key).getAsBoolean();
        return defaultVal;
    }

    public static JsonArray getJsonArray(JsonObject obj, String key)
    {
        if(obj.has(key)) return obj.get(key).getAsJsonArray();
        return new JsonArray();
    }

}
