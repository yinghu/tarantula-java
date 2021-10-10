package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class TournamentHistoryContext extends ResponseHeader {

    private List<Tournament.History> itemList;

    public TournamentHistoryContext(boolean successful, String message, List<Tournament.History> itemList){
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
            alist.add(v.toJson());
        });
        jsonObject.add("onList",alist);
        return jsonObject;
    }

    @Override
    public String toString(){
        return toJson().toString();
    }
}
