package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.Descriptor;

public class GameLobby {
    public Descriptor lobby;
    public Arena[] arenaList;

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",lobby.name());
        jsonObject.addProperty("tag",lobby.tag());
        jsonObject.addProperty("rank",lobby.accessRank());
        JsonArray jds = new JsonArray();
        for(Arena a: arenaList){
            JsonObject jd = new JsonObject();
            jd.addProperty("name",a.name());
            jd.addProperty("level",a.level);
            jd.addProperty("xp",a.xp);
            jd.addProperty("capacity",a.capacity);
            jd.addProperty("duration",a.duration);
            jd.addProperty("playMode",Arena.toPlayMode(a.playMode));
            jd.addProperty("disabled",a.disabled());
            jds.add(jd);
        }
        jsonObject.add("levels",jds);
        return jsonObject;
    }
}