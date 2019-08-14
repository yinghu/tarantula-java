package com.tarantula.test.ws;

import com.google.gson.GsonBuilder;
import com.tarantula.Response;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.bootstrap.TarantulaThreadFactory;
import com.tarantula.platform.presence.IndexContext;
import com.tarantula.platform.presence.PresenceContext;
import com.tarantula.platform.util.IndexContextDeserializer;
import com.tarantula.platform.util.OnAccessSerializer;
import com.tarantula.platform.util.PresenceContextDeserializer;
import com.tarantula.platform.util.ResponseDeserializer;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WebSocketTest {

    static GsonBuilder gsonBuilder;
    static {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(IndexContext.class,new IndexContextDeserializer());
        gsonBuilder.registerTypeAdapter(Response.class,new ResponseDeserializer());
        gsonBuilder.registerTypeAdapter(PresenceContext.class,new PresenceContextDeserializer());
        gsonBuilder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
    }
    static ExecutorService pool;

    public static void main(String[] args) throws Exception{
        int batch;
        int duration;
        String host;
        try{
            batch = Integer.parseInt(args[0]);
            duration = Integer.parseInt(args[1]);
            host = args[2];
        }
        catch (Exception ex){
            batch = 100;
            duration = 30;
            host = "10.0.0.234:8090";
        }
        pool = Executors.newFixedThreadPool(batch,new TarantulaThreadFactory("test-load"));
        CountDownLatch waiting = new CountDownLatch(1);
        AtomicInteger round = new AtomicInteger(0);
        AtomicLong total = new AtomicLong(0);
        AtomicLong st = new AtomicLong(0);
        AtomicLong ed = new AtomicLong(0);
        for(int i=0;i<batch;i++) {
            WebSocketSimulator wm = new WebSocketSimulator(false, host, gsonBuilder, round, waiting, UUID.randomUUID().toString(),total,st,ed);
            pool.execute(wm);
        }
        Thread.sleep(duration*1000);
        waiting.countDown();
        Thread.sleep(10000);
        pool.shutdown();
        System.out.println("Total web socket connections ["+round.get()+"] finished with total bytes received/duration ["+total.get()+"]["+(ed.get()-st.get())+"]");
    }

}
