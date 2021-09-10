package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Lobby;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.util.DescriptorSerializer;

public class GameServiceContext extends ResponseHeader {

    public Lobby lobby;

    public GameServiceContext(){
        this.successful = true;
    }

    public JsonObject toJson(){
        DescriptorSerializer serializer = new DescriptorSerializer();
        JsonObject jsonObject = (JsonObject) serializer.serialize(lobby.descriptor(), Descriptor.class,null);
        jsonObject.addProperty("successful",successful);
        JsonArray ja = new JsonArray();
        lobby.entryList().forEach((a)->{
            ja.add(serializer.serialize(a,Descriptor.class,null));
        });
        jsonObject.add("serviceList",ja);
        return jsonObject;
    }
}
