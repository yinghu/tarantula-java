package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.tarantula.Session;

import java.util.UUID;

public class SampleLoad {
    private final String host;
    private final String prefix;
    private final int size;
    private HttpCaller httpCaller;
    public SampleLoad(String host,String prefix,int size){
        this.host = host;
        this.prefix = prefix!=null?prefix: UUID.randomUUID().toString();
        this.size = size;
    }
    public void _init() throws Exception{
        httpCaller = new HttpCaller(host);
        httpCaller._init();
    }
    public void batch(){
        for(int i=0;i<size;i++){
            try{
                //register
                System.out.println(httpCaller.index());
                String _login = prefix+"-"+i;
                String[] headers = new String[]{
                        Session.TARANTULA_TAG,"index/user",
                        Session.TARANTULA_ACTION,"onRegister",
                        Session.TARANTULA_MAGIC_KEY,_login
                };
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("login",_login);
                jsonObject.addProperty("password","password");
                System.out.println(httpCaller.post("user/action",jsonObject.toString().getBytes(),headers));

            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws Exception{
        SampleLoad sampleLoad = new SampleLoad("http://10.0.0.234:8090",null,1000);
        sampleLoad._init();
        sampleLoad.batch();
    }
}

