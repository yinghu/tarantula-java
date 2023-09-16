package com.tarantula.test.integration;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;

import com.icodesoftware.util.TimeUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LMDBConcurrencyVerifier {

    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    static ExecutorService executorService;

    static LMDBDataStoreProvider lmdbDataStoreProvider;
    static TestMapStoreListener testMapStoreListener;
    static LocalDistributionIdGenerator idGenerator = new LocalDistributionIdGenerator(1,TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
    static {
        try{
            lmdbDataStoreProvider = new LMDBDataStoreProvider();
            lmdbDataStoreProvider.registerDistributionIdGenerator(idGenerator);
            lmdbDataStoreProvider.start();
            testMapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
            lmdbDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static void main(String[] args) throws Exception{
        int batch = 12;
        int nodeNumber = 12;
        executorService = Executors.newFixedThreadPool(nodeNumber);
        long st = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(nodeNumber);
        //int batch = 1_000;
        long[] prefixSet = new long[nodeNumber];
        for(int i=0;i<nodeNumber;i++){
            prefixSet[i]= idGenerator.id();
        }
        for(int i=0;i<nodeNumber;i++) {
            SnowflakeRunner runner = new SnowflakeRunner(prefixSet,batch,countDownLatch);
            executorService.execute(runner);
        }
        countDownLatch.await();
        int total = nodeNumber*batch;
        System.out.println("Duration Executed ["+((System.currentTimeMillis()-st)/1000.0)+"]");
        System.out.println("Expected keys ["+total+"]");
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("users");
        int[] ct={0};
        dataStore.backup().forEach((k,v)->{
            ct[0]++;
            return true;
        });
        System.out.println("Inserted keys ["+ct[0]+"]");
        executorService.shutdown();
    }
    static class SnowflakeRunner implements Runnable{

         final int batch;

        final long[] prefix;
        final CountDownLatch countDownLatch;

        final DataStore dataStore;
        public SnowflakeRunner(long[] prefix,int batch,CountDownLatch countDownLatch){
            this.prefix = prefix;
            this.batch = batch;
            this.countDownLatch = countDownLatch;
            this.dataStore = lmdbDataStoreProvider.createDataStore("users");
        }
        @Override
        public void run() {
            for(int i=0;i<batch;i++) {
                TestUser testUser = new TestUser();
                testUser.login("test");
                testUser.distributionId(prefix[i]);
                dataStore.createIfAbsent(testUser,false);
            }
            countDownLatch.countDown();
        }
    }
}
