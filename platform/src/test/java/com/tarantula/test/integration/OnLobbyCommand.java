package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tarantula.Session;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.presence.PresenceContext;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class OnLobbyCommand implements Callable<String> {

    private HashMap<String,String> _headers = new HashMap<>();
    private GsonBuilder gsonBuilder;
    private String host;
    private boolean secured;
    public OnLobbyCommand(boolean secured, String host, GsonBuilder gsonBuilder, PresenceContext presenceContext,String typeId){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        _headers.put(Session.TARANTULA_MAGIC_KEY,presenceContext.presence.systemId());
        _headers.put(Session.TARANTULA_TOKEN,presenceContext.presence.token());
        _headers.put(Session.TARANTULA_TAG,typeId);
    }
    public String call() throws Exception{
        OnAccessTrack u = new OnAccessTrack();
        u.header("typeId",_headers.get(Session.TARANTULA_TAG));
        String ret = new HTTPCaller(secured,host).doAction("onLobby","service/action",_headers,gsonBuilder.create().toJson(u).getBytes());
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(ret);
        String[] appId ={null};
        je.getAsJsonObject().get("gameList").getAsJsonArray().forEach((g)->{
            JsonObject jo = g.getAsJsonObject();
            if(jo.get("category").getAsString().equals("casino")){
                appId[0] = jo.get("applicationId").getAsString();
            }
        });
        return appId[0];
    }
}
