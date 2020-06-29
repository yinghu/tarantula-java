package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.tarantula.Session;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class SampleLoad {
    private final String host;
    private final String prefix;
    private final int size;
    private HttpCaller httpCaller;
    private ExecutorService pool;

    public SampleLoad(String host,String prefix,int size){
        this.host = host;
        this.prefix = prefix!=null?prefix: UUID.randomUUID().toString();
        this.size = size;
    }
    public void _init() throws Exception{
        httpCaller = new HttpCaller(host);
        httpCaller._init();
        pool = Executors.newFixedThreadPool(50);
    }
    public void batch() throws Exception{
        CountDownLatch batch = new CountDownLatch(size);
        AtomicLong timeRun = new AtomicLong(0);
        for(int i=0;i<size;i++){
            try{
                //register
                String _login = prefix+"-"+i;
                String[] headers = new String[]{
                        Session.TARANTULA_TAG,"index/user",
                        Session.TARANTULA_ACTION,"onRegister",
                        Session.TARANTULA_MAGIC_KEY,_login
                };
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("login",_login);
                jsonObject.addProperty("password","password");
                pool.execute(()->{
                    try{
                        long st = System.currentTimeMillis();
                        httpCaller.index();
                        httpCaller.post("user/action",jsonObject.toString().getBytes(),headers);
                        long ed = System.currentTimeMillis()-st;
                        timeRun.addAndGet(ed);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    finally {
                        batch.countDown();
                    }
                });
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
        batch.await();
        pool.shutdownNow();
        System.out.println("Average duration ["+(timeRun.get()/size)+"]");
    }
    public static void main(String[] args) throws Exception{
        SampleLoad sampleLoad = new SampleLoad("http://10.0.0.234:8090",null,50000);
        sampleLoad._init();
        sampleLoad.batch();
    }
}

