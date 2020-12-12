package com.icodesoftware.protocol;

import com.google.gson.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 12/12/2020.
 */
public class GameItem {
    public int typeId;
    public int sequence;
    public int sessionId;
    public AtomicInteger votes;

    public GameItem(int typeId,int sequence,int sessionId){
        this.typeId = typeId;
        this.sequence = sequence;
        this.sessionId = sessionId;
        this.votes = new AtomicInteger(0);
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("typeId",typeId);
        jsonObject.addProperty("sequence",sequence);
        jsonObject.addProperty("sessionId",sessionId);
        jsonObject.addProperty("votes",votes.get());
        return jsonObject;
    }
}
