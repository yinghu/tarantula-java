package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.ServiceProvider;

import java.lang.management.*;
import java.util.concurrent.TimeUnit;

public class JVMMonitor implements ServiceProvider {

    public static final String NAME = "JVMMonitor";

    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    private static final String CPU_USAGE ="CPUUsage";
    private static final String MEMORY_USAGE= "MemoryUsage";

    private long lastUpdates;
    private long lastUpTime;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void registerSummary(Summary summary){
        summary.registerCategory(CPU_USAGE);
        summary.registerCategory(MEMORY_USAGE);
    }
    @Override
    public void updateSummary(Summary summary){
        long current = 0;
        for(long tid : threadMXBean.getAllThreadIds()){
            current += threadMXBean.getThreadUserTime(tid);
        }
        long delta = TimeUnit.NANOSECONDS.toMillis(current-lastUpdates);
        lastUpdates = current;
        long time = runtimeMXBean.getUptime();
        long elasped = time-lastUpTime;
        lastUpTime = time;
        double per = ((double) delta/elasped)*100;
        summary.update(CPU_USAGE,per<0?0:per);
        summary.update(MEMORY_USAGE,memoryMXBean.getHeapMemoryUsage().getUsed()/1000000);
    }
}
