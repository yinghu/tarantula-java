package com.tarantula.admin;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class AdminSetupModule implements Module {

    private ApplicationContext context;



    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.context.log("Admin setup module started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "admin";
    }
}
