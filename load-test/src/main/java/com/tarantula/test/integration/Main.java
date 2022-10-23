package com.tarantula.test.integration;

import com.icodesoftware.util.TarantulaThreadFactory;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Updated by yinghu lu on 8/31/2019.
 */
public class Main {

    static ExecutorService pool;


    public static void main(String[] args) throws Exception{
        Properties properties = new Properties();
        properties.load(new FileInputStream("load.properties"));
        String host = properties.getProperty("host");
        int batch = Integer.parseInt(properties.getProperty("batch"));
        int poolSize = Integer.parseInt(properties.getProperty("pool.size"));
        String playerPrefix = properties.getProperty("player.prefix");
        boolean udpTested = Boolean.parseBoolean(properties.getProperty("test.udp"));
        int udpReceiveTimeout = Integer.parseInt(properties.getProperty("udp.receive.timeout"));
        long udpTestDuration = Long.parseLong(properties.getProperty("udp.test.duration"));
        LoadResult.playerPrefix = playerPrefix;
        LoadResult.host = host;
        LoadResult.poolSize = poolSize;
        LoadResult.batch = batch;
        LoadResult.startTime = LocalDateTime.now();
        LoadResult.udpTested = udpTested;
        LoadResult.udpReceiveTimeout = udpReceiveTimeout;
        LoadResult.udpTestDuration = udpTestDuration;
        //Player player = new Player("https://gameclustering.com",new CountDownLatch(1),"test_1000",1000);
        //player.run();
        //LoadResult.print();
        runSimulation(host,playerPrefix,batch,poolSize,udpTested,udpReceiveTimeout,udpTestDuration);
    }
    private static void runSimulation(String host,String playerPrefix,int batch,int poolSize,boolean udpTested,int timeout,long duration) throws Exception{
        pool = Executors.newFixedThreadPool(poolSize,new TarantulaThreadFactory("test-load"));
        int ix = 0;
        for(int i = 0;i<batch;i++){
            CountDownLatch waiting = new CountDownLatch(poolSize);
            for(int x=0;x<poolSize;x++){
                String uname = playerPrefix!=null?(playerPrefix+"-"+ix):UUID.randomUUID().toString();
                ix++;
                Player simulator = new Player(host,waiting,uname,x,udpTested,timeout,duration);
                pool.execute(simulator);
                Thread.sleep(4);
            }
            waiting.await();
        }
        pool.shutdown();
        LoadResult.print();
    }
}
