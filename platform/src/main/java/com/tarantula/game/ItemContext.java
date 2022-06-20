package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.admin.ConfigurableObjectSerializer;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.item.ConfigurableObject;

import java.util.List;

public class ItemContext extends ResponseHeader {

    private List<ConfigurableObject> itemList;

    public ItemContext(boolean successful,String message,List<ConfigurableObject> itemList){
        this.successful = successful;
        this.message = message;
        this.itemList = itemList;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",this.successful);
        jsonObject.addProperty("message",message);
        JsonArray alist = new JsonArray();
        ConfigurableObjectSerializer serializer = new ConfigurableObjectSerializer();
        itemList.forEach((v)->{
            alist.add(v.toJson());
        });
        jsonObject.add("itemList",alist);
        return jsonObject;
    }

    @Override
    public String toString(){
        return toJson().toString();
    }
}
