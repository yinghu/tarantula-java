package com.tarantula.platform.service;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class ApplicationMetrics implements Metrics, SchedulingTask {




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

    @Override
    public void setup(ServiceContext serviceContext) {

    }

    @Override
    public Statistics statistics() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
