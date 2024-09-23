package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TournamentOnNode {
    public String nodeId;
    public long[] onScheduled;

    public JsonObject toJson(){
        JsonObject resp = new JsonObject();
        resp.addProperty("nodeId",nodeId);
        JsonArray scanned = new JsonArray();
        for(long id : onScheduled){
            scanned.add(id);
        }
        resp.add("tournaments",scanned);
        return resp;
    }
}
