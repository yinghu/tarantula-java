package com.tarantula.game.module;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.Session;

public class StatisticsModule implements Module {
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {

    }

    @Override
    public String label() {
        return null;
    }
}
