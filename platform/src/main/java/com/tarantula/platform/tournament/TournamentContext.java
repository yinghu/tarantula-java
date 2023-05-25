package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.store.ShoppingItem;

import java.util.List;

public class TournamentContext extends ResponseHeader {

    private List<Tournament> itemList;

    public TournamentContext(boolean successful, String message, List<Tournament> itemList){
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
        jsonObject.add("_tournamentList",alist);
        return jsonObject;
    }

    @Override
    public String toString(){
        return toJson().toString();
    }
}
