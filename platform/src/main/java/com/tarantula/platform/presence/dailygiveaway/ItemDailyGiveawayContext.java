package com.tarantula.platform.presence.dailygiveaway;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.dailygiveaway.DailyGiveaway;

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
        jsonObject.addProperty("Successful",this.successful);
        if(!successful){
            jsonObject.addProperty("Message",message);
            return jsonObject;
        }
        JsonArray alist = new JsonArray();
        itemList.forEach((v)->{
            alist.add(v.toJson());
        });
        jsonObject.add("_itemList",alist);
        return jsonObject;
    }

    @Override
    public String toString(){
        return toJson().toString();
    }
}
