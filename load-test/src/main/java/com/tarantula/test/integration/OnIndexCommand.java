package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.Session;
import com.tarantula.platform.presence.IndexContext;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class OnIndexCommand implements Callable<IndexContext>{

    private HashMap<String,String> _headers = new HashMap<>();
    private GsonBuilder gsonBuilder;
    private String host;
    private boolean secured;
    public OnIndexCommand(boolean secured,String host,GsonBuilder gsonBuilder){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        _headers.put(Session.TARANTULA_TAG,"index");
    }
    @Override
    public IndexContext call() throws Exception {
        String ret = new HTTPCaller(secured,this.host).doAction("onIndex","user/index",_headers,null);
        return gsonBuilder.create().fromJson(ret.trim(),IndexContext.class);
    }
}
