package com.tarantula.platform.service;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.platform.TarantulaContext;

public class ApplicationMetrics implements MetricsListener, SchedulingTask {

    public final static String DATA_STORE_COUNT = "dataStoreCount";

    public ApplicationMetrics(TarantulaContext tarantulaContext){

    }


    @Override
    public void onUpdated(String s, double v) {

    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {

    }
}
