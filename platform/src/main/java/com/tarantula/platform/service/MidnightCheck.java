package com.tarantula.platform.service;

import com.tarantula.SchedulingTask;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.util.SystemUtil;

public class MidnightCheck implements SchedulingTask {

    private final TarantulaContext tarantulaContext;
    public MidnightCheck(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }

    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        //return 10*60*1000;
        return SystemUtil.toMidnight();
    }

    @Override
    public void run() {
        this.tarantulaContext.atMidnight();
    }
}
