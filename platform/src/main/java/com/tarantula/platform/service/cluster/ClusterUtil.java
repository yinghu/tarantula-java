package com.tarantula.platform.service.cluster;

import java.util.concurrent.Callable;

public class ClusterUtil {

    public static CallResult call(int retries,long retryInterval,Callable<Object> runnable){
        CallResult ret = new CallResult();
        for(int i=0;i<retries;i++){
            try{
                ret.result = runnable.call();
                ret.successful = true;
                break;
            }catch (Exception ex){
                ret.retries++;
                ret.exception = ex;
                waitForRetry(retryInterval);
            }
        }
        return ret;
    }
    public static void waitForRetry(long retryInterval){
        try{Thread.sleep(retryInterval);}catch (Exception ex){}
    }

    public static class CallResult{

        public CallResult(){
            successful = false;
            retries = 0;
        }
        public Object result;
        public int retries;

        public boolean successful;

        public Exception exception;
    }

}
