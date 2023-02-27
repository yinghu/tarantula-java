package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;


public class GameServiceProxyModule implements Module {

    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        Module serviceProxy = this.gameServiceProvider.serviceModule(session.name());
        if(serviceProxy != null) {
            serviceProxy.onRequest(session,payload);
        }
        else{
            session.write(JsonUtil.toSimpleResponse(false,"service module not available").getBytes());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("Game Service Module Started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }
}
