package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Lobby;
import com.tarantula.platform.presence.IndexContext;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Updated by yinghu on 10/7/2018.
 */
public class IndexContextDeserializer implements JsonDeserializer<IndexContext> {

    public IndexContext deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        IndexContext ic = new IndexContext();
        ic = (IndexContext)new ResponseDeserializer(ic).deserialize(jsonElement,type,jsonDeserializationContext);
        LobbyDeserializer ld = new LobbyDeserializer();
        ArrayList<Lobby> _list = new ArrayList();
        jo.get("lobbyList").getAsJsonArray().forEach((JsonElement j)->{
            _list.add(ld.deserialize(j,type,jsonDeserializationContext));
        });
        ic.lobbyList = _list;
        return ic;
    }
}
