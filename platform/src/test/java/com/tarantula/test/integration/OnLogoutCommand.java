package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.Descriptor;
import com.tarantula.Response;
import com.tarantula.Session;
import com.tarantula.platform.presence.PresenceContext;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class OnLogoutCommand implements Callable<Response> {

    private HashMap<String,String> _headers = new HashMap<>();
    private GsonBuilder gsonBuilder;
    private String host;
    private boolean secured;
    public OnLogoutCommand(boolean secured,String host,GsonBuilder gsonBuilder, PresenceContext presenceContext){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        Descriptor descriptor = presenceContext.lobbyList.get(0).entryList().get(0);
        _headers.put(Session.TARANTULA_MAGIC_KEY,presenceContext.presence.systemId());
        _headers.put(Session.TARANTULA_TOKEN,presenceContext.presence.token());
        _headers.put(Session.TARANTULA_APPLICATION_ID,descriptor.applicationId());
        _headers.put(Session.TARANTULA_TAG,descriptor.tag());
    }
    public Response call() throws Exception{
        String ret = new HTTPCaller(secured,host).doAction("onAbsence","service/action",_headers,"{}".getBytes());
        return gsonBuilder.create().fromJson(ret,Response.class);
    }
}
