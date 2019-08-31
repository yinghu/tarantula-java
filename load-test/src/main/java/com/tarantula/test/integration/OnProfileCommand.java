package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.Session;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.presence.PresenceContext;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class OnProfileCommand implements Callable<String> {

    private HashMap<String,String> _headers = new HashMap<>();
    private GsonBuilder gsonBuilder;
    private String host;
    private boolean secured;
    public OnProfileCommand(boolean secured, String host, GsonBuilder gsonBuilder, PresenceContext presenceContext){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
         presenceContext.lobbyList.forEach((b)->{
             if(b.descriptor().typeId().equals("profile")){
                 b.entryList().forEach((d)->{
                     if(d.tag().equals("profile")){
                         _headers.put(Session.TARANTULA_MAGIC_KEY,presenceContext.presence.systemId());
                         _headers.put(Session.TARANTULA_TOKEN,presenceContext.presence.token());
                         _headers.put(Session.TARANTULA_APPLICATION_ID,d.applicationId());
                         _headers.put(Session.TARANTULA_TAG,d.tag());
                     }
                 });
             }
         });
    }
    public String call() throws Exception{
        OnAccessTrack u = new OnAccessTrack();
        u.header("systemId",_headers.get(Session.TARANTULA_MAGIC_KEY));
        String ret = new HTTPCaller(secured,host).doAction("onProfile","service/action",_headers,gsonBuilder.create().toJson(u).getBytes());
        return ret;
    }
}
