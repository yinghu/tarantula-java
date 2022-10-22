package com.tarantula.test.integration;

import com.icodesoftware.util.TarantulaThreadFactory;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Updated by yinghu lu on 8/31/2019.
 */
public class Main {

    static ExecutorService pool;


    public static void main(String[] args) throws Exception{
        LoadResult.startTime = LocalDateTime.now();
        Player player = new Player("https://gameclustering.com",new CountDownLatch(1),"test_1000",1000);
        player.run();
        LoadResult.print();
        //runSimulation(args);
    }
    private static void runSimulation(String[] args) throws Exception{
        LoadResult.startTime = LocalDateTime.now();
        int batch;
        int poolSize;
        String host;
        String prefix =null;

        try{
            batch = Integer.parseInt(args[0]);
            poolSize = Integer.parseInt(args[1]);
            host = args[2];
            prefix = args[3];
        }catch (Exception ex){
            batch = 10;
            poolSize = 10;
            host = null;
            //prefix = "test";
        }
        if(host==null){
            host = "https://gameclustering.com";
        }
        LoadResult.batch = batch;
        LoadResult.poolSize = poolSize;
        LoadResult.host = host;
        LoadResult.playerPrefix = prefix!=null?prefix:"random";

        pool = Executors.newFixedThreadPool(poolSize,new TarantulaThreadFactory("test-load"));
        int ix = 0;
        for(int i = 0;i<batch;i++){
            CountDownLatch waiting = new CountDownLatch(poolSize);
            for(int x=0;x<poolSize;x++){
                String uname = prefix!=null?(prefix+"-"+ix):UUID.randomUUID().toString();
                ix++;
                Player simulator = new Player(host,waiting,uname,x);
                pool.execute(simulator);
                Thread.sleep(4);
            }
            waiting.await();
        }
        pool.shutdown();
        LoadResult.print();
    }
}
