package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.Metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;

public class JVMMonitor implements SchedulingTask {

    private Metrics performanceMetrics;

    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private long timerCountDown = 100;
    public final static long timerInternal = 10000;
    private ApplicationContext context;
    public JVMMonitor(ApplicationContext context, Metrics performanceMetrics, long timerCountDown){
        this.context = context;
        this.performanceMetrics = performanceMetrics;
        if(timerCountDown>0) this.timerCountDown = timerCountDown;
    }
    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return timerInternal;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        double mused = (double)memoryMXBean.getHeapMemoryUsage().getUsed()/1073741824D;
        this.context.log(String.format("Mem used : %.2f GB",mused),OnLog.WARN);
        int tc = threadMXBean.getThreadCount();
        this.context.log("Thread count->"+tc,OnLog.WARN);
        performanceMetrics.onUpdated(PerformanceMetrics.PERFORMANCE_VM_MEMORY_COUNT,mused);
        performanceMetrics.onUpdated(PerformanceMetrics.PERFORMANCE_VM_THREAD_COUNT,threadMXBean.getThreadCount());
        long cpu = 0;
        for(long tid : threadMXBean.getAllThreadIds()){
            cpu += threadMXBean.getThreadCpuTime(tid);
        }
        this.context.log("CPU usage->"+cpu,OnLog.WARN);
        performanceMetrics.onUpdated(AbstractMetrics.PERFORMANCE_VM_CPU_USAGE_COUNT,cpu);
        timerCountDown--;
        if(timerCountDown <= 0){
            this.context.log("JVM metrics monitoring has stopped", OnLog.WARN);
            return;
        }
        context.schedule(this);
    }
}
