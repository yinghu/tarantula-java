package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobbyProxy;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.service.metrics.GameClusterMetrics;
import com.tarantula.platform.util.OnAccessDeserializer;

public class

GameLobbyModule extends ModuleHeader{

    private GameLobbyProxy gameLobby;
    private GsonBuilder builder;
    private Descriptor application;

    @Override
    public void onJoin(Session session) throws Exception{
        if(application.tournamentEnabled() && session.tournamentId()!=null && (!gameServiceProvider.tournamentServiceProvider().available(session.tournamentId()))){
            session.write(JsonUtil.toSimpleResponse(false,"no tournament available,please try later").getBytes());
            return;
        }
        Rating rating = gameServiceProvider.presenceServiceProvider().rating(session);
        Stub stub = gameLobby.join(session,rating);
        session.write(stub.toJson().toString().getBytes());
        if(!stub.joined()) return;
        gameServiceProvider.presenceServiceProvider().onPlay(session.systemId());
        gameServiceProvider.gameServiceProvider().onJoined(session);
    }

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onStartGame")){
            gameServiceProvider.gameServiceProvider().startGame(session,payload);
        }
        else if(session.action().equals("onUpdateGame")){
            gameServiceProvider.gameServiceProvider().updateGame(session,payload);
        }
        else if(session.action().equals("onEndGame")){
            gameServiceProvider.gameServiceProvider().endGame(session,payload);
        }
        else if(session.action().equals("onLeave")){
            boolean left = gameLobby.leave(session);
            session.write(JsonUtil.toSimpleResponse(left,"left room").getBytes());
            if(left)gameServiceProvider.gameServiceProvider().onLeft(session);
        }
        else if(session.action().equals("onValidate")){
            this.context.log("check game session",OnLog.WARN);
            this.gameLobby.validate(session);
        }
        else if(session.action().equals("onTest")){
            if(this.context.validator().role(session.distributionId()).accessControl()< AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
            Rating rating = gameServiceProvider.presenceServiceProvider().rating(session);
            rating.level = this.context.descriptor().accessRank()*100-99;
            Stub stub = gameLobby.join(session,rating);
            session.write(stub.toJson().toString().getBytes());
            if(stub.joined()) {
                gameServiceProvider.presenceServiceProvider().onPlay(session.systemId());
                this.gameServiceProvider.gameServiceProvider().onJoined(session);
            }
        }
        else if(session.action().equals("onTestScore")){
            if(this.context.validator().role(session.distributionId()).accessControl()< AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
        }

        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.application = this.context.descriptor();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.gameLobby = new GameLobbyProxy();
        this.gameLobby.setup(context);
        this.gameLobby.start();
        this.gameServiceProvider.lobbyServiceProvider().registerConfigurableListener(this.context.descriptor(),this.gameLobby);
        this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Game lobby started on tag ["+context.descriptor().tag()+"]",OnLog.WARN);
    }
    @Override
    public void clear() {
        super.clear();
        this.gameServiceProvider.lobbyServiceProvider().unregisterConfigurableListener(context.descriptor().tag());
        try{ gameLobby.shutdown();}catch (Exception ex){}
        this.context.log("clear->"+this.context.descriptor().tag(),OnLog.WARN);
    }

}
