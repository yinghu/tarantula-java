package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Access;
import com.tarantula.Descriptor;
import com.tarantula.Lobby;
import com.tarantula.platform.presence.IndexContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 8/24/19
 */
public class IndexContextSerializer implements JsonSerializer<IndexContext> {

    public JsonElement serialize(IndexContext presenceContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject pc = (JsonObject)new ResponseSerializer().serialize(presenceContext,type,jsonSerializationContext);
        if(presenceContext.view!=null){
            pc.add("view",new OnViewSerializer().serialize(presenceContext.view,type,jsonSerializationContext));
        }
        if(presenceContext.lobbyList!=null){
            DescriptorSerializer ser = new DescriptorSerializer();
            JsonArray blist = new JsonArray();
            for(Lobby lobby : presenceContext.lobbyList){
                JsonObject jlobby = new JsonObject();
                jlobby.add("descriptor",ser.serialize(lobby.descriptor(),type,jsonSerializationContext));
                JsonArray jlist = new JsonArray();
                if(lobby.descriptor().accessMode()== Access.PUBLIC_ACCESS_MODE){
                    for(Descriptor d : lobby.entryList()){
                        //add application list
                        jlist.add(ser.serialize(d,type,jsonSerializationContext));
                    }
                }
                jlobby.add("applications",jlist);
                blist.add(jlobby);
            }
            pc.add("lobbyList",blist);
        }
        return pc;
    }
}
