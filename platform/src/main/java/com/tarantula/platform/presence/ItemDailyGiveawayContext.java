package com.tarantula.platform.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.achievement.Achievement;

import java.util.List;

public class ItemDailyGiveawayContext extends ResponseHeader {

    private List<DailyGiveaway> itemList;

    public ItemDailyGiveawayContext(boolean successful, String message, List<DailyGiveaway> itemList){
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
