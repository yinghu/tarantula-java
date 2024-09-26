package com.tarantula.platform.inbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.ResponseHeader;


import java.util.Comparator;
import java.util.List;

public class GlobalItemGrantList extends ResponseHeader {
    public List<GlobalItemGrantEvent> grantEventList;

    public GlobalItemGrantList(List<GlobalItemGrantEvent> grantEventList){
        grantEventList.sort((Comparator.comparing(GlobalItemGrantEvent::getDateCreated)));
        this.grantEventList = grantEventList;
    }

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        if(grantEventList!=null){
            JsonArray klist = new JsonArray();
            grantEventList.forEach(k->klist.add(k.toJson()));
            jo.add("globalGrantEvent",klist);
        }
        return jo;
    }
}
