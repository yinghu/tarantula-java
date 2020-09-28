package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Descriptor;
import com.tarantula.Lobby;
import com.tarantula.platform.presence.PresenceContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 5/25/2020
 */
public class PresenceContextSerializer implements JsonSerializer<PresenceContext> {

    public JsonElement serialize(PresenceContext presenceContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject pc = (JsonObject)new ResponseSerializer().serialize(presenceContext,type,jsonSerializationContext); //new JsonObject();
        if(presenceContext.presence!=null){
            pc.add("presence",new OnSessionSerializer().serialize(presenceContext.presence,type,jsonSerializationContext));
        }
        if(presenceContext.lobbyList!=null){
            DescriptorSerializer ser = new DescriptorSerializer();
            JsonArray blist = new JsonArray();
            for(Lobby lobby : presenceContext.lobbyList){
                JsonObject jlobby = new JsonObject();
                jlobby.add("descriptor",ser.serialize(lobby.descriptor(),type,jsonSerializationContext));
                JsonArray jlist = new JsonArray();
                for(Descriptor d : lobby.entryList()){
                    //add application list
                    jlist.add(ser.serialize(d,type,jsonSerializationContext));
                }
                jlobby.add("applications",jlist);
                blist.add(jlobby);
            }
            pc.add("lobbyList",blist);
        }
        if(presenceContext.roleList!=null){
            JsonArray rlist = new JsonArray();
            RoleSerializer roleSerializer = new RoleSerializer();
            presenceContext.roleList.forEach((r)->{
                rlist.add(roleSerializer.serialize(r,type,jsonSerializationContext));
            });
            pc.add("roleList",rlist);
        }
        if(presenceContext.googleClientId!=null){
            pc.addProperty("googleClientId",presenceContext.googleClientId);
        }
        if(presenceContext.stripeClientId!=null){
            pc.addProperty("stripeClientId",presenceContext.stripeClientId);
        }
        if(presenceContext.access!=null){
            pc.add("access",new AccessSerializer().serialize(presenceContext.access,type,jsonSerializationContext));
        }
        if(presenceContext.account!=null){
            pc.add("account",new AccountSerializer().serialize(presenceContext.account,type,jsonSerializationContext));
        }
        if(presenceContext.subscription!=null){
            pc.add("subscription",new SubscriptionSerializer().serialize(presenceContext.subscription,type,jsonSerializationContext));
        }
        if(presenceContext.connection!=null){
            pc.add("connection",new ConnectionSerializer().serialize(presenceContext.connection,type,jsonSerializationContext));
        }
        return pc;
    }
}
