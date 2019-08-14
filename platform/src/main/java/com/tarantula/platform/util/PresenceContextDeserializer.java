package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Lobby;
import com.tarantula.platform.presence.PresenceContext;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Updated by yinghu on 7/12/19
 */
public class PresenceContextDeserializer implements JsonDeserializer<PresenceContext> {

    public PresenceContext deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        PresenceContext ic = new PresenceContext();
        ic = (PresenceContext) new ResponseDeserializer(ic).deserialize(jsonElement,type,jsonDeserializationContext);
        if(ic.successful()){
            if(jo.has("presence")){
                ic.presence = new PresenceDeserializer().deserialize(jo.get("presence"),type,jsonDeserializationContext);
            }
            LobbyDeserializer ld = new LobbyDeserializer();
            ArrayList<Lobby> _list = new ArrayList();
            if(jo.has("lobbyList")){
                jo.get("lobbyList").getAsJsonArray().forEach((JsonElement j)->{
                    _list.add(ld.deserialize(j,type,jsonDeserializationContext));
                });
            }
            ic.lobbyList = _list;
            if(jo.has("connection")){
                ic.connection = new ConfigurationDeserializer().deserialize(jo.get("connection"),type,jsonDeserializationContext);
            }
        }
        return ic;
    }
}
