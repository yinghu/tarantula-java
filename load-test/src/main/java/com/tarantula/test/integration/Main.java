package com.tarantula.test.integration;

import com.tarantula.platform.bootstrap.TarantulaThreadFactory;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Updated by yinghu lu on 8/31/2019.
 */
public class Main {

    static ExecutorService pool;


    public static void main(String[] args) throws Exception{
        Player caller = new Player(false,"localhost:8090",new CountDownLatch(1),UUID.randomUUID().toString(),new DemoSync(),jsonObject -> System.out.println(jsonObject));
        caller.run();
        //runSimulation(args);
    }
    private static void runSimulation(String[] args) throws Exception{
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
            batch = 10;
            psize = 100;
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
                Player simulator = new Player(secured,host,waiting,uname,new DemoSync(),jsonObject -> round.incrementAndGet());
                pool.execute(simulator);
                Thread.sleep(4);
            }
            waiting.await();
        }
        System.out.println("Total rounds ["+round.get()+"] timed ["+(System.currentTimeMillis()-st)+"]");
        pool.shutdown();
    }



}
