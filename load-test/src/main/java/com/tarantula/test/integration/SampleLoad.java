package com.tarantula.test.integration;


import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.util.HttpCaller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public SampleLoad(String host, String prefix, int size){
        this.host = host;
        this.prefix = prefix!=null?prefix: UUID.randomUUID().toString();
        this.size = size;
    }
    public void _init() throws Exception{
        httpCaller = new HttpCaller(host);
        httpCaller._init();
        pool = Executors.newFixedThreadPool(100);
    }
    public void device() throws Exception{
        CountDownLatch batch = new CountDownLatch(size);
        AtomicLong timeRun = new AtomicLong(0);
        long start = System.currentTimeMillis();
        for(int i=0;i<size;i++){
            try{
                //register
                String _login = prefix+"-"+i;
                String[] headers = new String[]{
                        Session.TARANTULA_TAG,"index/user",
                        Session.TARANTULA_ACTION,"onDevice",
                        Session.TARANTULA_MAGIC_KEY,_login
                };
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("deviceId",_login);
                //jsonObject.addProperty("password","password");
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
        System.out.println("Average duration ["+(timeRun.get()/size)+"] with total time ["+((System.currentTimeMillis()-start)/1000)+"]");
    }
    public void register() throws Exception{
        CountDownLatch batch = new CountDownLatch(size);
        AtomicLong timeRun = new AtomicLong(0);
        long start = System.currentTimeMillis();
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
                        //httpCaller.index();
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
        System.out.println("Average duration ["+(timeRun.get()/size)+" ms ] with total time ["+((System.currentTimeMillis()-start)/1000)+" seconds ]");

    }
    public static void main(String[] args) throws Exception{
        SampleLoad sampleLoad = new SampleLoad("http://192.168.1.15:8090",null,30000);
        sampleLoad._init();
        sampleLoad.register();
        //BDS/baf7f6189ba0423f8b87940260083668/history_userCreationCount_hourly_2022_253
        //PresenceFetcher presenceFetcher  =new PresenceFetcher("http://192.168.1.5:8090");
        //presenceFetcher._init();
        //OnSession onSession = presenceFetcher.login("mop","mop");
        //System.out.println(onSession.token());
        //PresenceFetcher presenceFetcher1  =new PresenceFetcher("http://192.168.1.9:8090");
        //presenceFetcher1._init();
        //presenceFetcher1.play(onSession.token());
        //LocalDateTime t11 = LocalDateTime.now().plusHours(11);
        //System.out.println(t11);
        //System.out.println(t11.getDayOfYear());
        //long dur = TimeUtil.durationToNextHour(t11);
        //LocalDateTime n1 = t11.plusSeconds(dur/1000);
        //System.out.println(n1);
        //System.out.println(n1.getHour());
        //System.out.println(n1.getHour());
        //LocalDateTime end = LocalDate.parse("2022-01-01").atTime(LocalTime.MIDNIGHT);//Sun
        //System.out.println(end.getDayOfYear());
        //System.out.println(end.getYear());
        //System.out.println(end.getHour());
        //System.out.println(end.minusHours(1).getYear());
        //System.out.println(end.minusHours(1).getDayOfYear());
        //System.out.println(end.minusHours(1).getHour());
    }

}

