package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.ServiceProvider;

import java.lang.management.*;


public class JVMMonitor implements ServiceProvider {

    public static final String NAME = "JVMMonitor";

    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static  com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();

    private static final String PROCESS_CPU_USAGE ="ProcessCPUUsage";

    private static final String MEMORY_USAGE= "MemoryUsage";
    private static final String THREAD_COUNT = "ThreadCount";

    private long lastUpTime;
    private long lastProcessTimed;
    private static int totalProcessors;
    static {
        totalProcessors = operatingSystemMXBean.getAvailableProcessors();
    }

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
        summary.registerCategory(PROCESS_CPU_USAGE);
        summary.registerCategory(MEMORY_USAGE);
        summary.registerCategory(THREAD_COUNT);
        lastUpTime = System.nanoTime();
        lastProcessTimed = operatingSystemMXBean.getProcessCpuTime();
    }
    @Override
    public void updateSummary(Summary summary){

        long processTimed = processTimed();
        long time = System.nanoTime();
        double elapsed = (time-lastUpTime)*totalProcessors;
        lastUpTime = time;
        double perProcess = (processTimed/elapsed)*100;
        summary.update(PROCESS_CPU_USAGE,Double.parseDouble(String.format("%.2f",perProcess)));
        summary.update(MEMORY_USAGE,memoryMXBean.getHeapMemoryUsage().getUsed()/1000000);
        summary.update(THREAD_COUNT,threadMXBean.getThreadCount());
    }

    private long processTimed(){
        long cur = operatingSystemMXBean.getProcessCpuTime();
        long delta = cur-lastProcessTimed;
        lastProcessTimed = cur;
        return delta;
    }
}
