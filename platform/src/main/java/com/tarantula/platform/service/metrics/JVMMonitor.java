package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.ServiceProvider;

import java.lang.management.*;
import java.util.HashMap;

public class JVMMonitor implements ServiceProvider {

    public static final String NAME = "JVMMonitor";

    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static  com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    private static final String THREAD_CPU_USAGE ="ThreadCPUUsage";
    private static final String PROCESS_CPU_USAGE ="ProcessCPUUsage";

    private static final String MEMORY_USAGE= "MemoryUsage";
    private static final String THREAD_COUNT = "ThreadCount";

    private long lastUpTime;
    private long lastProcessTimed;
    private static int totalProcessors;
    static {
        totalProcessors = operatingSystemMXBean.getAvailableProcessors();
    }

    private HashMap<Long,Long> ths = new HashMap<>();
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
        summary.registerCategory(THREAD_CPU_USAGE);
        summary.registerCategory(PROCESS_CPU_USAGE);
        summary.registerCategory(MEMORY_USAGE);
        summary.registerCategory(THREAD_COUNT);
        lastUpTime = System.nanoTime();
        lastProcessTimed = operatingSystemMXBean.getProcessCpuTime();
    }
    @Override
    public void updateSummary(Summary summary){

        long threadTimed = threadTimed();
        long processTimed = processTimed();
        long time = System.nanoTime();
        double elapsed = (time-lastUpTime)*totalProcessors;
        lastUpTime = time;
        double perThread = (threadTimed/elapsed)*100;
        double perProcess = (processTimed/elapsed)*100;
        summary.update(THREAD_CPU_USAGE,Double.parseDouble(String.format("%.2f",perThread)));
        summary.update(PROCESS_CPU_USAGE,Double.parseDouble(String.format("%.2f",perProcess)));
        summary.update(MEMORY_USAGE,memoryMXBean.getHeapMemoryUsage().getUsed()/1000000);
        summary.update(THREAD_COUNT,threadMXBean.getThreadCount());
    }
    private long threadTimed(){
        long[] current = {0};
        for(long tid : threadMXBean.getAllThreadIds()){
            ths.compute(tid,(k,v)->{
                long th = threadMXBean.getThreadCpuTime(tid);
                if(th == -1) return null;
                long last = v==null? 0 : v;
                current[0] += (th-last);
                return th;
            });
        }
        return current[0];
    }
    private long processTimed(){
        long cur = operatingSystemMXBean.getProcessCpuTime();
        long delta = cur-lastProcessTimed;
        lastProcessTimed = cur;
        return delta;
    }
}
