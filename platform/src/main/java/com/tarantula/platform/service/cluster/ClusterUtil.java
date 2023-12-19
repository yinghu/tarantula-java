package com.tarantula.platform.service.cluster;

import com.icodesoftware.service.MetricsListener;
import com.tarantula.platform.service.metrics.ClusterMetrics;

import java.util.concurrent.Callable;

public class ClusterUtil {

    public static CallResult call(int retries, long retryInterval, Callable<Object> callable, MetricsListener metricsListener){
        CallResult ret = new CallResult();
        for(int i=0;i<retries;i++){
            try{
                ret.result = callable.call();
                ret.successful = true;
                break;
            }catch (Exception ex){
                ret.retries++;
                ret.exception = ex;
                waitForRetry(retryInterval);
            }
        }
        metricsListener.onUpdated(ret.successful? ClusterMetrics.CLUSTER_REMOTE_CALL_COUNT : ClusterMetrics.CLUSTER_REMOTE_CALL_FAILURE_COUNT,1);
        if(ret.retries>0) metricsListener.onUpdated(ClusterMetrics.CLUSTER_REMOTE_RETRY_COUNT,ret.retries);
        return ret;
    }
    private static void waitForRetry(long retryInterval){
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
