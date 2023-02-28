package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.MatchMakingComparator;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.lobby.LobbyItem;
import com.tarantula.platform.service.metrics.GameClusterMetrics;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MatchMakingModule implements Module,Configurable.Listener<LobbyItem>,Tournament.Listener {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Descriptor> mLobby;
    private GameServiceProvider gameServiceProvider;
    private GsonBuilder builder;
    private String lobbyId;
    private int maxRank;
    private DeploymentServiceProvider deploymentServiceProvider;
    private String registerKey;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //check Rating to match the game zone to join 
        if(session.action().equals("onPlay")){
            Rating rating = this.gameServiceProvider.rating(session.systemId());
            int mix = rating.rank > maxRank? maxRank : rating.rank;
            Descriptor lobby = mLobby.get(mix);
            if(lobby != null) {
                if(lobby.entryCost() > 0 && !this.context.presence(session).transact(lobby.entryCost()*(-1))){
                    session.write(JsonUtil.toSimpleResponse(false,"not enough balance").getBytes());
                }
                else{
                    Module module = this.gameServiceProvider.serviceModule(lobby.tag());
                    module.onJoin(session);
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"no lobby available").getBytes());
            }
            this.gameServiceProvider.onUpdated(GameClusterMetrics.GAME_JOIN_COUNT,1);
        }
        else if(session.action().equals("onLeave")){
            String[] query = session.name().split("#");
            session.name(query[1]);
            Module module = this.gameServiceProvider.serviceModule(query[0]);
            module.onRequest(session,payload);
        }
        else if(session.action().equals("onTestTournament")){
            if(this.context.validator().role(session.systemId()).accessControl()< AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
            session.tournamentId(session.name());
            session.clientId("device_"+session.stub());
            session.name("web_device");
            session.action("onPlay");
            Rating rating = this.gameServiceProvider.rating(session.systemId());
            int mix = rating.rank>maxRank?maxRank:rating.rank;
            Descriptor lobby = mLobby.get(mix);
            if(lobby!=null) {
                Response response = context.presence(session).onPlay(session, lobby);
                if (response != null) session.write(this.builder.create().toJson(response).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"no lobby available").getBytes());
            }
        }

        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.mLobby = new ConcurrentHashMap<>();//max matching level
        lobbyId = this.context.descriptor().typeId().replace("data","lobby");
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId().replace("data","service"));
        this.maxRank = ((Number)this.gameServiceProvider.configuration().property("matchMakingMaxRank")).intValue();
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.registerKey = deploymentServiceProvider.registerConfigurableListener(OnLobby.TYPE,new OnLobbyListener());
        this.gameServiceProvider.lobbyServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.gameServiceProvider.tournamentServiceProvider().registerTournamentListener(this);
        context.log("Started match making module on ->"+this.context.descriptor().tag(), OnLog.WARN);
    }
    @Override
    public void clear() {
        this.deploymentServiceProvider.unregisterConfigurableListener(registerKey);
        this.context.log("clear->"+this.context.descriptor().tag(),OnLog.WARN);
    }
    private Lobby listLobby(){
        Lobby lobby = this.context.lobby(lobbyId);
        List<Descriptor> alist = lobby.entryList();
        Collections.sort(alist,new MatchMakingComparator());
        int start = 1;
        for(Descriptor a : alist){
            if(a.disabled()) continue;
            mLobby.put(a.accessRank(),a);
            for(int i = start;i<a.accessRank();i++){
                mLobby.put(i,a);
            }
            start++;
        }
        if(start>1&&start<maxRank){
            Descriptor lastZone = mLobby.get(start-1);
            for(int i = start;i<=maxRank;i++){
                mLobby.put(i,lastZone);
            }
        }
        else{
            this.context.log("No lobby Entries for Game->"+context.descriptor().typeId(),OnLog.WARN);
        }
        mLobby.forEach((k,v)->{
            context.log("Access Rank ["+k+"] registered on ["+v.accessRank()+"]",OnLog.WARN);
        });
        return lobby;
    }

    @Override
    public void tournamentStarted(Tournament tournament) {
        this.context.log(tournament.distributionKey()+" STARTED",OnLog.WARN);
    }

    @Override
    public void tournamentClosed(Tournament tournament) {
        this.context.log(tournament.distributionKey()+" CLOSED",OnLog.WARN);
    }

    @Override
    public void tournamentEnded(Tournament tournament) {
        this.context.log(tournament.distributionKey()+" ENDED",OnLog.WARN);
    }

    private class OnLobbyListener implements Configurable.Listener<OnLobby>,Lobby.Listener{

        public OnLobbyListener(){
        }
        @Override
        public void onUpdated(OnLobby onLobby) {
            if(onLobby.typeId().equals(lobbyId)){
                if(onLobby.closed()) {
                    mLobby.clear();
                    context.log("Lobby ["+onLobby.typeId()+"] is going to be offline",OnLog.WARN);
                    deploymentServiceProvider.unregisterConfigurableListener(registerKey);
                    return;
                }
                context.log("Lobby ["+onLobby.typeId()+"] is going to be online",OnLog.WARN);
                listLobby().addListener(this);
            }
        }

        @Override
        public void onLobby(Descriptor descriptor) {
            context.log("Lobby Updated : disable["+descriptor.disabled()+"] rank["+descriptor.accessRank()+"]", OnLog.WARN);
            if(descriptor.accessRank()>0&&descriptor.accessRank()<=maxRank){
                mLobby.clear();
                listLobby();
            }
        }
    }
}
