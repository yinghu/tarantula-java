package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;

public class GameLobbyModule implements Module, Connection.OnConnectionListener {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GameServiceProvider gameServiceProvider;
    private GameZone gameZone;
    @Override
    public void onJoin(Session session, Module.OnUpdate onUpdate) throws Exception{
        Rating rating = gameServiceProvider.rating(session.systemId());
        Stub stub = gameZone.join(rating);
    }

    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.deploymentServiceProvider = applicationContext.serviceProvider(DeploymentServiceProvider.NAME);
        this.gameServiceProvider = applicationContext.serviceProvider(context.descriptor().typeId().replace("lobby","service"));
        this.gameZone = this.gameServiceProvider.zone(this.context.descriptor());
        this.gameZone.start(this.context);
        this.deploymentServiceProvider.register(this.gameZone);
        if(this.gameZone.connected()){
            this.deploymentServiceProvider.registerOnConnectionListener(this);
        }
        this.context.log("Game lobby started on tag ["+context.descriptor().tag()+"]",OnLog.WARN);
    }

    //connection listener
    @Override
    public String lobbyTag() {
        return this.context.descriptor().tag();
    }

    @Override
    public void onConnection(Session session) {

    }
}
