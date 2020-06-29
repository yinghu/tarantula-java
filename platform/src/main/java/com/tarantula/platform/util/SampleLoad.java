package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.tarantula.Session;

public class SampleLoad {
    private final String host;
    private final String prefix;
    private final int size;
    private HttpCaller httpCaller;
    public SampleLoad(String host,String prefix,int size){
        this.host = host;
        this.prefix = prefix;
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
                String[] headers = new String[]{
                        Session.TARANTULA_TAG,"index/user",
                        Session.TARANTULA_ACTION,"onRegister",
                        Session.TARANTULA_MAGIC_KEY,"test1"
                };
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("login","test1");
                jsonObject.addProperty("password","test");
                System.out.println(httpCaller.post("user/action",jsonObject.toString().getBytes(),headers));

            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws Exception{
        SampleLoad sampleLoad = new SampleLoad("https://gameclustering.com","",1);
        sampleLoad._init();
        sampleLoad.batch();
    }
}

