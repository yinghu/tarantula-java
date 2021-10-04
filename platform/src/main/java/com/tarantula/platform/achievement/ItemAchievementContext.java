package com.tarantula.platform.achievement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.item.Application;

import java.util.List;

public class ItemAchievementContext extends ResponseHeader {

    private List<Achievement> itemList;

    public ItemAchievementContext(boolean successful, String message, List<Achievement> itemList){
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
        itemList.forEach((v)->{
            alist.add(v.configurableHeader().toJson());
        });
        jsonObject.add("itemList",alist);
        return jsonObject;
    }

    @Override
    public String toString(){
        return toJson().toString();
    }
}
