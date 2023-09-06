package com.tarantula.test.integration;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LMDBLoadVerifier {

    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    static ExecutorService executorService;

    static final int nodeNumber = 12;

    static {
        executorService = Executors.newFixedThreadPool(nodeNumber);
    }

    static LMDBDataStoreProvider lmdbDataStoreProvider;
    static TestMapStoreListener testMapStoreListener;
    static {
        try{
            lmdbDataStoreProvider = new LMDBDataStoreProvider();
            //lmdbDataStoreProvider.configure(new HashMap<>(){{
                //put("dir","target/lmdb");
            //}});
            lmdbDataStoreProvider.start();
            testMapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
            lmdbDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static void main(String[] args) throws Exception{
        long st = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(nodeNumber);
        int batch = 1_000;
        String[] prefixSet = new String[nodeNumber];
        for(int i=0;i<nodeNumber;i++){
            prefixSet[i]="user_"+i+"_";
        }
        for(int i=0;i<nodeNumber;i++) {
            SnowflakeRunner runner = new SnowflakeRunner(prefixSet[i],batch,new SnowflakeIdGenerator(i,TimeUtil.epochMillisecondsFromMidnight(2020,1,1)),countDownLatch);
            executorService.execute(runner);
        }
        countDownLatch.await();
        int total = nodeNumber*batch;
        System.out.println("Duration Executed ["+((System.currentTimeMillis()-st)/1000.0)+"]");
        System.out.println("Expected keys ["+total+"]");
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("users");
        int[] ct={0};
        dataStore.backup().list((k,h,v)->{
            ct[0]++;
            return true;
        });
        System.out.println("Inserted keys ["+ct[0]+"]");
        executorService.shutdown();
    }
    static class SnowflakeRunner implements Runnable{

        final SnowflakeIdGenerator snowflakeIdGenerator;
        final int batch;

        final String prefix;
        final CountDownLatch countDownLatch;

        final DataStore dataStore;
        public SnowflakeRunner(String prefix,int batch,SnowflakeIdGenerator snowflakeIdGenerator,CountDownLatch countDownLatch){
            this.prefix = prefix;
            this.batch = batch;
            this.snowflakeIdGenerator = snowflakeIdGenerator;
            this.countDownLatch = countDownLatch;
            this.dataStore = lmdbDataStoreProvider.createDataStore("users");
        }
        @Override
        public void run() {
            for(int i=0;i<batch;i++) {
                TestUser testUser = new TestUser(prefix+i,testMapStoreListener.snowflakeIdGenerator.snowflakeId());
                dataStore.create(testUser);
            }
            countDownLatch.countDown();
        }
    }
}
