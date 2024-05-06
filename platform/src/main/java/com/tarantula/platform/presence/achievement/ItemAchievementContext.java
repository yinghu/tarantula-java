package com.tarantula.platform.presence.achievement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.ResponseHeader;
import java.util.List;

public class ItemAchievementContext extends ResponseHeader {

    private List<AchievementItem> itemList;

    public ItemAchievementContext(boolean successful, String message, List<AchievementItem> itemList){
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
