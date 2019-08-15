package com.tarantula.platform.marketplace;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class AdminModule implements Module {

    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        session.write(payload,this.label());
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.context.log("Tarantula Admin Module started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "admin";
    }
}
