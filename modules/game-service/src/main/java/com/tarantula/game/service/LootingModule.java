package com.tarantula.game.service;


import com.icodesoftware.*;
import com.icodesoftware.Module;

public class LootingModule implements Module {

    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] payload, Module.OnUpdate onUpdate) throws Exception {
        session.write(payload,label());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Tarantula Loot service->"+context.descriptor().distributionKey(), OnLog.WARN);
    }

    @Override
    public String label() {
        return "looting";
    }

}
