package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.util.OnAccessDeserializer;

public class GameLobbyModule implements Module, Connection.OnConnectionListener {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GameServiceProvider gameServiceProvider;
    private GameZone gameZone;
    private GsonBuilder builder;
    private Descriptor application;
    @Override
    public void onJoin(Session session, Module.OnUpdate onUpdate) throws Exception{
        if(application.tournamentEnabled()&&(!gameServiceProvider.tournamentServiceProvider().available(session.tournamentId()))){
            session.write(toMessage("no tournament available,please try later",false).getBytes());
            return;
        }
        Rating rating = gameServiceProvider.rating(session.systemId());
        Stub stub = gameZone.join(session,rating);
        session.write(stub.toJson().toString().getBytes());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onLeave")){
            gameZone.leave(session.systemId());
            session.write(toMessage("left room",true).getBytes());
        }
        else if(session.action().equals("onUpdate")){
            Statistics statistics = gameServiceProvider.statistics(session.systemId());
            statistics.entry("kills").update(1).update();
            statistics.entry("wins").update(1).update();
            statistics.entry("stars").update(1).update();
            session.write(toMessage("updated",true).getBytes());
            if(application.tournamentEnabled()){
                this.gameZone.update(session.systemId());
            }
        }
        else if(session.action().equals("onTest")){
            if(application.tournamentEnabled()&&(!gameServiceProvider.tournamentServiceProvider().available(session.tournamentId()))){
                session.write(toMessage("no tournament available,please try later",false).getBytes());
            }
            else{
                OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
                Rating rating = this.gameServiceProvider.rating(session.systemId());
                rating.xpLevel = onAccess.stub();
                Stub stub = gameZone.join(session,rating);
                session.write(stub.toJson().toString().getBytes());
                gameZone.leave(session.systemId());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }
    @Override
    public void onTimer(Module.OnUpdate update){
        this.gameZone.onTimer(update);
    }
    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.application = this.context.descriptor();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.deploymentServiceProvider = applicationContext.serviceProvider(DeploymentServiceProvider.NAME);
        this.gameServiceProvider = applicationContext.serviceProvider(context.descriptor().typeId().replace("lobby","service"));
        this.gameZone = this.gameServiceProvider.zone(this.context.descriptor());
        this.gameZone.start(this.context);
        this.deploymentServiceProvider.register(this.gameZone);
        gameServiceProvider.roomServiceProvider().registerGameZone(this.gameZone);
        if(this.gameZone.connected()){
            this.deploymentServiceProvider.registerOnConnectionListener(this);
        }
        this.context.log("Game lobby started on tag ["+context.descriptor().tag()+"]",OnLog.WARN);
    }
    @Override
    public void clear() {
        this.deploymentServiceProvider.release(gameZone);
        this.gameServiceProvider.roomServiceProvider().releaseGameZone(gameZone);
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
