package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.util.OnAccessDeserializer;

public class GameLobbyModule implements Module, Connection.OnConnectionListener {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GameServiceProvider gameServiceProvider;
    private GameLobby gameLobby;
    private GsonBuilder builder;
    private Descriptor application;
    @Override
    public void onJoin(Session session, Module.OnUpdate onUpdate) throws Exception{
        if(application.tournamentEnabled()&&session.tournamentId()!=null&&(!gameServiceProvider.tournamentServiceProvider().available(session.tournamentId()))){
            session.write(toMessage("no tournament available,please try later",false).getBytes());
            return;
        }
        Rating rating = gameServiceProvider.rating(session.systemId());
        Stub stub = gameLobby.join(session,rating);
        session.write(stub.toJson().toString().getBytes());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onLeave")){
            gameLobby.leave(session);
            session.write(toMessage("left room",true).getBytes());
        }
        else if(session.action().equals("onUpdate")){
            this.gameLobby.update(session,payload,onUpdate);
        }
        else if(session.action().equals("onList")){
            this.gameLobby.list(session);
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }
    @Override
    public void onTimer(Module.OnUpdate update){
        this.gameLobby.onTimer(update);
    }
    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.application = this.context.descriptor();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.deploymentServiceProvider = applicationContext.serviceProvider(DeploymentServiceProvider.NAME);
        this.gameServiceProvider = applicationContext.serviceProvider(context.descriptor().typeId().replace("lobby","service"));
        this.gameLobby = this.gameServiceProvider.lobby(this.context.descriptor());
        this.gameLobby.setup(context);
        this.gameLobby.start();
        this.context.log("Game lobby started on tag ["+context.descriptor().tag()+"]",OnLog.WARN);
    }
    @Override
    public void clear() {
        try{ gameLobby.shutdown();}catch (Exception ex){}
        this.context.log("clear->"+this.context.descriptor().tag(),OnLog.WARN);
    }
    //connection listener
    @Override
    public String lobbyTag() {
        return this.context.descriptor().tag();
    }

    @Override
    public void onConnection(Session session) {

    }

    private String toMessage(String msg, boolean successful){
        return JsonUtil.toSimpleResponse(successful,msg);
    }
}
