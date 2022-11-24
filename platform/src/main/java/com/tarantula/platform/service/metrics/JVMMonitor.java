package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceProvider;

import java.lang.management.*;
import java.util.concurrent.TimeUnit;

public class JVMMonitor implements ServiceProvider {

    private Metrics performanceMetrics;

    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    private long timerCountDown = 100;
    public final static long timerInternal = 10000;
    private ApplicationContext context;

    public JVMMonitor(ApplicationContext context, Metrics performanceMetrics, long timerCountDown){
        this.context = context;
        this.performanceMetrics = performanceMetrics;
        if(timerCountDown>0) this.timerCountDown = timerCountDown;
    }



    //@Override
    public void run() {
        double mused = (double)memoryMXBean.getHeapMemoryUsage().getUsed()/1073741824D;
        this.context.log(String.format("Mem used : %.2f GB",mused),OnLog.WARN);
        int tc = threadMXBean.getThreadCount();
        this.context.log("Thread count->"+tc,OnLog.WARN);
        performanceMetrics.onUpdated(PerformanceMetrics.PERFORMANCE_VM_MEMORY_COUNT,mused);
        performanceMetrics.onUpdated(PerformanceMetrics.PERFORMANCE_VM_THREAD_COUNT,threadMXBean.getThreadCount());
        double cpu = 0;
        for(long tid : threadMXBean.getAllThreadIds()){
            cpu += TimeUnit.NANOSECONDS.toMillis(threadMXBean.getThreadCpuTime(tid));
        }
        this.context.log("Uptime 1->"+runtimeMXBean.getUptime(),OnLog.WARN);
        this.context.log("CPU Time->"+ cpu,OnLog.WARN);
        double rt = runtimeMXBean.getUptime();
        double cpuUsage = cpu/rt;
        this.context.log(String.format("CPU usage-> %.2f",cpuUsage),OnLog.WARN);
        this.context.log("CPU usage 2->"+operatingSystemMXBean.getSystemLoadAverage(),OnLog.WARN);

        performanceMetrics.onUpdated(AbstractMetrics.PERFORMANCE_VM_CPU_USAGE_COUNT,cpu);
        timerCountDown--;
        if(timerCountDown <= 0){
            this.context.log("JVM metrics monitoring has stopped", OnLog.WARN);
            return;
        }
        //context.schedule(this);
    }

    @Override
    public String name() {
        return "JVMMonitor";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
