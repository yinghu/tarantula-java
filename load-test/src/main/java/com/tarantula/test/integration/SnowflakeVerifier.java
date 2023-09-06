package com.tarantula.test.integration;

import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnowflakeVerifier {

    static ConcurrentHashMap<Long,Long> verifier = new ConcurrentHashMap<>();
    static ExecutorService executorService;

    static final int nodeNumber = 12;

    static {
        executorService = Executors.newFixedThreadPool(nodeNumber);
    }
    public static void main(String[] args) throws Exception{
        long st = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(nodeNumber);
        int batch = 1_000_000;
        for(int i=0;i<nodeNumber;i++) {
            SnowflakeRunner runner = new SnowflakeRunner(batch,new SnowflakeIdGenerator(i,TimeUtil.epochMillisecondsFromMidnight(2020,1,1)),countDownLatch);
            executorService.execute(runner);
        }
        countDownLatch.await();
        int total = nodeNumber*batch;
        System.out.println("Duration Executed ["+((System.currentTimeMillis()-st)/1000.0)+"]");
        System.out.println("Expected keys ["+total+"]");
        System.out.println(verifier.size());
        executorService.shutdown();
    }
    static class SnowflakeRunner implements Runnable{

        final SnowflakeIdGenerator snowflakeIdGenerator;
        final int batch;

        final CountDownLatch countDownLatch;
        public SnowflakeRunner(int batch,SnowflakeIdGenerator snowflakeIdGenerator,CountDownLatch countDownLatch){
            this.batch = batch;
            this.snowflakeIdGenerator = snowflakeIdGenerator;
            this.countDownLatch = countDownLatch;
        }
        @Override
        public void run() {
            for(int i=0;i<batch;i++) {
                if (verifier.putIfAbsent(snowflakeIdGenerator.snowflakeId(), 1L) != null){
                    System.out.println("Duplicated Key And Stop");
                    break;
                }
            }
            countDownLatch.countDown();
        }
    }
}
