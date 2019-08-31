package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.Descriptor;
import com.tarantula.Response;

import com.tarantula.Session;
import com.tarantula.platform.OnAccessTrack;


import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class OnRegistrationCommand implements Callable<Response> {

    private HashMap<String,String> _headers = new HashMap<>();
    private GsonBuilder gsonBuilder;
    private String userName;
    private String password;
    private String host;
    private boolean secured;
    public OnRegistrationCommand(boolean secured,String host,GsonBuilder gsonBuilder, String uname, String pwd, Descriptor descriptor){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        this.userName = uname;
        this.password = pwd;
        _headers.put(Session.TARANTULA_MAGIC_KEY,this.userName);
        _headers.put(Session.TARANTULA_APPLICATION_ID,descriptor.applicationId());
        _headers.put(Session.TARANTULA_TAG,descriptor.tag());
    }
    public Response call() throws Exception{
        OnAccessTrack u = new OnAccessTrack();
        u.header("nickname",this.userName);
        u.header("password",password);
        u.header("login",this.userName);
        u.header("avatar",this.userName);
        String ret = new HTTPCaller(secured,host).doAction("onRegister","user/action",_headers,gsonBuilder.create().toJson(u).getBytes());
        return gsonBuilder.create().fromJson(ret,Response.class);
    }
}
