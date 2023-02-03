package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.tarantula.game.GameLobby;
import com.tarantula.game.service.GameServiceProvider;


public class GameServiceModule implements Module {

    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onService")){
            GameLobby.ServiceProxy serviceProxy = this.gameServiceProvider.serviceProxy((short) 1);
            //serviceProxy.onService()
            //List<String> list = this.gameServiceProvider.presenceServiceProvider().friendList(session.systemId());
            //session.write(toJson(list).toString().getBytes());
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
