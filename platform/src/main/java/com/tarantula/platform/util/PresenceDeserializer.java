package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.OnSession;
import com.tarantula.platform.OnSessionTrack;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 7/12/2019
 */
public class PresenceDeserializer implements JsonDeserializer<OnSession> {

    public OnSession deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        String systemId = jo.get("systemId").getAsString();
        OnSession pres = new OnSessionTrack();
        pres.systemId(systemId);
        if(jo.has("token")){
            pres.token(jo.get("token").getAsString());
        }
        if(jo.has("stub")){
            pres.stub(jo.get("stub").getAsInt());
        }
        if(jo.has("ticket")){
            pres.ticket(jo.get("ticket").getAsString());
        }
        return pres;
    }
}
