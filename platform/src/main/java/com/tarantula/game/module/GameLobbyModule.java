package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobbyProxy;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.service.metrics.GameClusterMetrics;
import com.tarantula.platform.util.OnAccessDeserializer;

public class GameLobbyModule implements Module{

    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private GameLobbyProxy gameLobby;
    private GsonBuilder builder;
    private Descriptor application;
    @Override
    public void onJoin(Session session) throws Exception{
        if(application.tournamentEnabled()&&session.tournamentId()!=null&&(!gameServiceProvider.tournamentServiceProvider().available(session.tournamentId()))){
            session.write(toMessage("no tournament available,please try later",false).getBytes());
            return;
        }
        Rating rating = gameServiceProvider.rating(session.systemId());
        Stub stub = gameLobby.join(session,rating);
        session.write(stub.toJson().toString().getBytes());
        //this.gameServiceProvider.presenceServiceProvider().onPlay(session.systemId());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {

        if(session.action().equals("onUpdate")){
            this.gameLobby.update(session,payload);
        }
        else if(session.action().equals("onValidate")){
            this.context.log("check game session",OnLog.WARN);
            this.gameLobby.validate(session);
        }
        else if(session.action().equals("onList")){
            this.gameLobby.list(session);
        }
        else if(session.action().equals("onTest")){
            if(this.context.validator().role(session.systemId()).accessControl()< AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
            session.clientId("device_"+session.stub());
            session.name("web_device");
            Rating rating = gameServiceProvider.rating(session.systemId());
            rating.level = this.context.descriptor().accessRank()*100-99;
            Stub stub = gameLobby.join(session,rating);
            session.write(stub.toJson().toString().getBytes());
            this.gameServiceProvider.onUpdated(GameClusterMetrics.GAME_JOIN_COUNT,1);
        }
        else if(session.action().equals("onTestScore")){
            if(this.context.validator().role(session.systemId()).accessControl()< AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
            this.gameLobby.update(session,payload);
        }
        else if(session.action().equals("onTestList")){
            if(this.context.validator().role(session.systemId()).accessControl()< AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
            this.gameLobby.list(session);
        }
        else if(session.action().equals("onLeave")){
            session.write(toMessage("left room",true).getBytes());
            gameLobby.leave(session);
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.application = this.context.descriptor();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.gameServiceProvider = applicationContext.serviceProvider(context.descriptor().typeId().replace("lobby","service"));
        this.gameLobby = new GameLobbyProxy();
        this.gameLobby.setup(context);
        this.gameLobby.start();
        this.gameServiceProvider.lobbyServiceProvider().registerConfigurableListener(this.context.descriptor(),this.gameLobby);
        this.context.log("Game lobby started on tag ["+context.descriptor().tag()+"]",OnLog.WARN);
    }
    @Override
    public void clear() {
        this.gameServiceProvider.lobbyServiceProvider().unregisterConfigurableListener(context.descriptor().tag());
        try{ gameLobby.shutdown();}catch (Exception ex){}
        this.context.log("clear->"+this.context.descriptor().tag(),OnLog.WARN);
    }

    private String toMessage(String msg, boolean successful){
        return JsonUtil.toSimpleResponse(successful,msg);
    }


}
