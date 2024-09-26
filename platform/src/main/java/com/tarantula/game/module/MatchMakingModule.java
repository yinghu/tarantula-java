package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.MatchMakingComparator;
import com.tarantula.platform.AccessControl;
import com.icodesoftware.util.ResponseHeader;
import com.tarantula.platform.lobby.LobbyItem;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MatchMakingModule extends ModuleHeader implements Configurable.Listener<LobbyItem> {


    private ConcurrentHashMap<Integer,Descriptor> mLobby;

    private GsonBuilder builder;
    private String lobbyId;
    private int maxRank;
    private DeploymentServiceProvider deploymentServiceProvider;
    private String registerKey;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //check Rating to match the game zone to join 
        if(session.action().equals("onPlay")){
            Rating rating = this.gameServiceProvider.presenceServiceProvider().rating(session);
            int mix = rating.rank() > maxRank? maxRank : rating.rank();
            Descriptor lobby = mLobby.get(mix);
            if(lobby != null) {
                Module module = this.gameServiceProvider.serviceModule(lobby.tag());
                module.onJoin(session);
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"no lobby available").getBytes());
            }
        }
        else if(session.action().equals("onUpdateGame")){
            gameServiceProvider.gameServiceProvider().updateGame(session,payload);
        }
        else if(session.action().equals("onLeave")){
            String[] query = session.name().split("#");
            session.name(query[1]);
            Module module = this.gameServiceProvider.serviceModule(query[0]);
            module.onRequest(session,payload);
        }

        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.mLobby = new ConcurrentHashMap<>();//max matching level
        lobbyId = this.context.descriptor().typeId().replace("data","lobby");
        this.maxRank = this.gameServiceProvider.gameCluster().maxLobbyCount();//((Number)this.gameServiceProvider.configuration().property("matchMakingMaxRank")).intValue();
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.registerKey = deploymentServiceProvider.registerConfigurableListener(OnLobby.TYPE,new OnLobbyListener());
        this.gameServiceProvider.lobbyServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        //this.gameServiceProvider.tournamentServiceProvider().registerTournamentListener(this);
        context.log("Started match making module on ->"+this.context.descriptor().tag(), OnLog.WARN);
    }
    @Override
    public void clear() {
        super.clear();
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
            gameServiceProvider.presenceServiceProvider().onLobby(a);
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
            context.log("Lobby Updated : disable ["+descriptor.disabled()+"] rank ["+descriptor.accessRank()+"]", OnLog.WARN);
            if(descriptor.accessRank()>0&&descriptor.accessRank()<=maxRank){
                mLobby.clear();
                listLobby();
            }
        }
    }
}
