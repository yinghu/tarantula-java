package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.bootstrap.TarantulaThreadFactory;

import com.tarantula.platform.presence.IndexContext;
import com.tarantula.platform.util.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class Main {

    static ExecutorService pool;
    static GsonBuilder gsonBuilder;
    static {
        gsonBuilder = new GsonBuilder();
        //gsonBuilder.registerTypeAdapter(IndexContext.class,new IndexContextDeserializer());
        gsonBuilder.registerTypeAdapter(Response.class,new ResponseDeserializer());
        //gsonBuilder.registerTypeAdapter(PresenceContext.class,new PresenceContextDeserializer());
        //gsonBuilder.registerTypeAdapter(MarketplaceContext.class,new ShoppingPackageDeserializer());
        gsonBuilder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
    }
    static MessageDigest messageDigest;

    public static void mainc(String[] args) throws Exception{
        System.setProperty("javax.net.debug","ssl");
        //System.setProperty("javax.net.ssl.keyStore","C:\\Users\\i7\\.keystore");
        OnIndexCommand cmd = new OnIndexCommand(true,"realnumber.net",gsonBuilder);
        IndexContext ic = cmd.call();
        System.out.println(gsonBuilder.create().toJson(ic));
       // System.out.println(SystemUtil.toMidnight()/(1000*60*60));
        //System.out.println(SystemUtil.toString(new String[]{"A","B"}));
    }

    public static void main(String[] args) throws Exception{
        messageDigest = MessageDigest.getInstance(TokenValidator.MDA);
        runSimulation(args);
    }



    static MessageDigest messageDigest(){
        try{
            return (MessageDigest) messageDigest.clone();
        }catch (Exception ex){
            return null;
        }
    }


    public static void runSimulation(String[] args) throws Exception{
        long st = System.currentTimeMillis();
        AtomicInteger round = new AtomicInteger(0);
        int batch;
        int psize;
        String host;
        String prefix =null;
        boolean secured =false;
        try{
            batch = Integer.parseInt(args[0]);
            psize = Integer.parseInt(args[1]);
            host = args[2];
            prefix = args[3];
        }catch (Exception ex){
            //ex.printStackTrace();
            batch = 100;
            psize = 50;
            host = null;
            ///prefix = "test";
        }
        if(host==null){
            //host = "10.0.0.29:8090";
            host = "10.0.0.234:8090";
            secured = false;
        }
        System.out.println("Load test on ["+host+"] with batch/pool/prefix size ["+batch+"/"+psize+"/"+prefix+"]");
        pool = Executors.newFixedThreadPool(psize,new TarantulaThreadFactory("test-load"));
        int ix = 0;
        for(int i = 0;i<batch;i++){
            CountDownLatch waiting = new CountDownLatch(psize);
            for(int x=0;x<psize;x++){
                String uname = prefix!=null?(prefix+"-"+ix):UUID.randomUUID().toString();
                ix++;
                Simulator simulator = new Simulator(secured,host,gsonBuilder,round,waiting,uname);
                pool.execute(simulator);
            }
            waiting.await();
        }
        System.out.println("Total rounds ["+round.get()+"] timed ["+(System.currentTimeMillis()-st)+"]");
        pool.shutdown();
    }



}
