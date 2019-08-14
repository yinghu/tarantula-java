package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Descriptor;
import com.tarantula.Lobby;
import com.tarantula.platform.DefaultLobby;
import java.lang.reflect.Type;

/**
 * Updated by yinghu on 18/6/2019.
 */
public class LobbyDeserializer implements JsonDeserializer<Lobby> {


    public Lobby deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        DescriptorDeserializer df = new DescriptorDeserializer();
        Lobby lob = new DefaultLobby(df.deserialize(jo.get("descriptor"),type,jsonDeserializationContext));
        //lob.descriptor(df.deserialize(jo.get("descriptor"),type,jsonDeserializationContext));
        jo.get("applications").getAsJsonArray().forEach((JsonElement j)->{
            Descriptor descriptor = df.deserialize(j,type,jsonDeserializationContext);
            descriptor.distributionKey(descriptor.applicationId());
            lob.addEntry(descriptor);
        });
        return lob;
    }
}
