package com.tarantula.platform.service;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.TarantulaContext;

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
        return TimeUtil.toMidnight();
    }

    @Override
    public void run() {
        this.tarantulaContext.atMidnight();
    }
}
