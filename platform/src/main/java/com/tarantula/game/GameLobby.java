package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Descriptor;

public class GameLobby {
    public Descriptor lobby;
    public PVPZone zone;

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",zone.name!=null?zone.name:lobby.name());
        jsonObject.addProperty("tag",lobby.tag());
        jsonObject.addProperty("rank",lobby.accessRank());
        jsonObject.addProperty("capacity",zone.capacity);
        jsonObject.addProperty("joinsOnStart",zone.joinsOnStart);
        jsonObject.addProperty("duration",zone.roundDuration/60000);
        jsonObject.addProperty("playMode",zone.toPlayMode());
        jsonObject.addProperty("levelLimit",zone.levelLimit>0?zone.levelLimit:lobby.capacity());
        jsonObject.addProperty("configLabel",zone.index());
        jsonObject.addProperty("disabled",lobby.disabled());
        JsonArray jds = new JsonArray();
        for(Arena a: zone.arenas){
            JsonObject jd = new JsonObject();
            jd.addProperty("name",a.name());
            jd.addProperty("level",a.level);
            jd.addProperty("xp",a.xp);
            jd.addProperty("capacity",a.capacity);
            jd.addProperty("joinsOnStart",a.joinsOnStart);
            jd.addProperty("duration",a.duration/60000);
            jd.addProperty("configLabel",a.index());
            jd.addProperty("disabled",a.disabled());
            jds.add(jd);
        }
        jsonObject.add("levels",jds);
        return jsonObject;
    }
}