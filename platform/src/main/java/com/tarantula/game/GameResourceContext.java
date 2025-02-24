package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.TRResponse;
import com.tarantula.platform.resource.GameResource;

import java.util.List;

public class GameResourceContext extends TRResponse {

    private List<GameResource> itemList;

    public GameResourceContext(boolean successful, String message, List<GameResource> itemList){
        this.successful = successful;
        this.message = message;
        this.itemList = itemList;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",this.successful);
        jsonObject.addProperty("Message",message);
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
