package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tarantula.Descriptor;

import java.util.ArrayList;
import java.util.List;

public class AdminSetupObject extends AdminObject{

    public List<Descriptor> list = new ArrayList();

    public AdminSetupObject(String label){
        super(label);
    }
    public JsonElement setup(){
        JsonObject jo = super.setup().getAsJsonObject();
        JsonArray ja = new JsonArray();
        list.forEach((d)->{
             ja.add(d.typeId());
        });
        jo.add("list",ja);
        return jo;
    }
}
