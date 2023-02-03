package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobby;
import com.tarantula.game.service.GameServiceProvider;


public class GameServiceModule implements Module {

    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onService")){
            GameLobby.ServiceProxy serviceProxy = this.gameServiceProvider.serviceProxy(session.serviceId());
            byte[] response = serviceProxy.onService(session,payload);
            if(response !=null) {
                session.write(response);
            }
            else{
                session.write(JsonUtil.toSimpleResponse(true,"service updated").getBytes());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action());
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
