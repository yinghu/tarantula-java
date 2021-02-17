package com.tarantula.game.service;


import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;

public class TournamentAdminModule implements Module {

    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        session.write(payload,label());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Tarantula Tournament Admin Module Started->"+context.descriptor().distributionKey(), OnLog.WARN);
    }

    @Override
    public String label() {
        return "looting";
    }

}
