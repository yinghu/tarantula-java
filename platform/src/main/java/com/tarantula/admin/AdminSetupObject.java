package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.Descriptor;
import com.tarantula.platform.util.DescriptorSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AdminSetupObject extends AdminObject{

    public List<Descriptor> list = new ArrayList();

    public AdminSetupObject(String message,String label){
        super(label);
        this.message = message;
    }
    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jo = super.setup(type,jsonSerializationContext).getAsJsonObject();
        JsonArray ja = new JsonArray();
        DescriptorSerializer ds = new DescriptorSerializer();
        list.forEach((d)->{
             ja.add(ds.serialize(d,type,jsonSerializationContext));
        });
        jo.add("list",ja);
        return jo;
    }
}
