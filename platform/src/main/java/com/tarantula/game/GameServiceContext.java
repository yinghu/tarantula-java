package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.Lobby;
import com.tarantula.platform.ResponseHeader;

public class GameServiceContext extends ResponseHeader {

    public Lobby lobby;

    public GameServiceContext(){
        this.successful = true;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("typeId",lobby.descriptor().typeId());
        jsonObject.addProperty("name",lobby.descriptor().name());
        JsonArray ja = new JsonArray();
        lobby.entryList().forEach((a)->{
            JsonObject jsc = new JsonObject();
            jsc.addProperty("name",a.name());
            jsc.addProperty("tag",a.tag());
            ja.add(jsc);
        });
        jsonObject.add("serviceList",ja);
        return jsonObject;
    }
}
