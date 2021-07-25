package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Descriptor;

public class GameLobby {
    public Descriptor lobby;
    public GameZone zone;

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonObject jzon = new JsonObject();
        jzon.addProperty("name",zone.name()!=null?zone.name():lobby.name());
        jzon.addProperty("tag",lobby.tag());
        jzon.addProperty("rank",lobby.accessRank());
        jzon.addProperty("capacity",zone.capacity());
        jzon.addProperty("joinsOnStart",zone.joinsOnStart());
        jzon.addProperty("duration",zone.roundDuration()/60000);
        jzon.addProperty("levelLimit",zone.levelLimit()>0?zone.levelLimit():lobby.capacity());
        jzon.addProperty("playMode",zone.playMode());
        jzon.addProperty("disabled",lobby.disabled());
        jsonObject.add("zone",jzon);
        JsonArray jds = new JsonArray();
        for(Arena a: zone.arenas()){
            JsonObject jd = new JsonObject();
            jd.addProperty("name",a.name());
            jd.addProperty("level",a.level);
            jd.addProperty("xp",a.xp);
            jd.addProperty("capacity",a.capacity);
            jd.addProperty("joinsOnStart",a.joinsOnStart);
            jd.addProperty("duration",a.duration/60000);
            jd.addProperty("disabled",a.disabled());
            jds.add(jd);
        }
        jsonObject.add("levels",jds);
        return jsonObject;
    }
}