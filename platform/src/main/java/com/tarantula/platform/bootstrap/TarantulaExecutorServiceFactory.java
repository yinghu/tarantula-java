package com.tarantula.platform.bootstrap;

import com.icodesoftware.service.Serviceable;

import java.util.concurrent.*;

public class TarantulaExecutorServiceFactory {

    /**
	public static ExecutorService createExecutorService(String poolSetting){
        String[] settings = poolSetting.split(",");
        String tprefix = settings[0];
        int poolMin = Integer.parseInt(settings[1]);
        int poolMax = Integer.parseInt(settings[2]);
        int poolOverflow = Integer.parseInt(settings[3]);
        int poolIdle = Integer.parseInt(settings[4]);
        int poolWaitingSize = Integer.parseInt(settings[5]);
        BlockingQueue<Runnable> queue = poolWaitingSize>0?new ArrayBlockingQueue<>(poolWaitingSize):new SynchronousQueue<>();
        if(poolWaitingSize>0){
            return  new ThreadPoolExecutor(poolMin,poolMax,poolIdle,TimeUnit.SECONDS,queue,new TarantulaThreadFactory(tprefix),new TarantulaRetryExecutionHandler(tprefix,poolOverflow));
        }else{
            return  new ThreadPoolExecutor(poolMin,poolMax,poolIdle,TimeUnit.SECONDS,queue,new TarantulaThreadFactory(tprefix),new TarantulaRejectedExecutionHandler(tprefix,poolOverflow));
        }
    }**/
    public static void createExecutorService(String poolSetting,FixedPool fixedPool){
        String[] settings = poolSetting.split(",");
        String tprefix = settings[0];
        int poolMin = Integer.parseInt(settings[1]);
        int poolMax = Integer.parseInt(settings[2]);
        int poolOverflow = Integer.parseInt(settings[3]);
        int poolIdle = Integer.parseInt(settings[4]);
        int poolWaitingSize = Integer.parseInt(settings[5]);
        BlockingQueue<Runnable> queue = poolWaitingSize>0?new ArrayBlockingQueue<>(poolWaitingSize):new SynchronousQueue<>();
        if(poolWaitingSize>0) {
            TarantulaRetryExecutionHandler retryExecutionHandler = new TarantulaRetryExecutionHandler(tprefix,poolOverflow);
            fixedPool.on(new ThreadPoolExecutor(poolMin,poolMax,poolIdle,TimeUnit.SECONDS,queue,new TarantulaThreadFactory(tprefix),retryExecutionHandler),poolMin,retryExecutionHandler);
        }else{
            TarantulaRejectedExecutionHandler rejectedExecutionHandler = new TarantulaRejectedExecutionHandler(tprefix,poolOverflow);
            fixedPool.on(new ThreadPoolExecutor(poolMin,poolMax,poolIdle,TimeUnit.SECONDS,queue,new TarantulaThreadFactory(tprefix),rejectedExecutionHandler),poolMin,rejectedExecutionHandler);
        }
    }
    public static ScheduledExecutorService createScheduledExecutorService(String poolSetting){
        String[] settings = poolSetting.split(",");
        String tprefix = settings[0];
        int poolMin = Integer.parseInt(settings[1]);
        int poolOverflow = Integer.parseInt(settings[2]);
        return  new ScheduledThreadPoolExecutor(poolMin,new TarantulaThreadFactory(tprefix),new TarantulaRejectedExecutionHandler(tprefix,poolOverflow));
    }
    public interface FixedPool{
	    void on(ExecutorService executorService, int poolSize, Serviceable rejectHandler);
    }
}
