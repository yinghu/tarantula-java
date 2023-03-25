package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;


public class GameServiceProxyModule implements Module {

    private ApplicationContext context;
    private PlatformGameServiceProvider gameServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        String[] query = session.action().split("#");
        if(query[0].equals("onService")){
            GameServiceProxy proxy = this.gameServiceProvider.gameServiceProxy(Short.parseShort(query[1]));
            byte[] resp = proxy.onService(session,payload);
            session.write(resp!=null?resp:JsonUtil.toSimpleResponse(true,"").getBytes());
        }
        else if(query[0].equals("onModule")){
            session.action(query[1]);
            Module serviceProxy = this.gameServiceProvider.serviceModule(session.trackId());
            serviceProxy.onRequest(session,payload);
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
