package com.tarantula.game.module;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class StatisticsModule implements Module {
    private ApplicationContext context;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //fetch statistics from systemId
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.context.log("Statistics started", OnLog.WARN);
    }

    @Override
    public String label() {
        return "stats";
    }
}
