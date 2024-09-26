package com.tarantula.platform.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.JsonSerializable;

import java.util.List;

public class JsonSerializableContext<T extends JsonSerializable> implements JsonSerializable {

    private final List<T> content;

    public JsonSerializableContext(List<T> content){
        this.content = content;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        if(content==null || content.isEmpty()) {
            jsonObject.addProperty("Successful",false);
            jsonObject.addProperty("Message","content not available");
            return jsonObject;
        }
        jsonObject.addProperty("Successful",true);
        JsonArray payload = new JsonArray();
        content.forEach((t)->{
            payload.add(t.toJson());
        });
        jsonObject.add("_content",payload);
        return jsonObject;
    }

    @Override
    public String toString(){
        return toJson().toString();
    }

}
