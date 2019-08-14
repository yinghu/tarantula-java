package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.Descriptor;
import com.tarantula.OnAccess;
import com.tarantula.Session;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.presence.PresenceContext;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class OnPlayCommand implements Callable<String> {

    private HashMap<String,String> _headers = new HashMap<>();
    private GsonBuilder gsonBuilder;
    private String host;
    private boolean secured;
    private String applicationId;
    public OnPlayCommand(boolean secured, String host, GsonBuilder gsonBuilder, PresenceContext presenceContext,String applicationId){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        _headers.put(Session.TARANTULA_MAGIC_KEY,presenceContext.presence.systemId());
        _headers.put(Session.TARANTULA_TOKEN,presenceContext.presence.token());
        this.applicationId = applicationId;
        _headers.put(Session.TARANTULA_TAG,"presence");
    }
    public String call() throws Exception{
        OnAccess onAccess = new OnAccessTrack();
        onAccess.applicationId(applicationId);
        onAccess.accessMode(2);
        String js = gsonBuilder.create().toJson(onAccess);
        return new HTTPCaller(secured,host).doAction("onPlay","service/action",_headers,js.getBytes());
        //return gsonBuilder.create().fromJson(ret,PresenceContext.class);
    }
}
