package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.MatchMakingComparator;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MatchMakingModule implements Module, Lobby.Listener {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Descriptor> mLobby;
    private GameServiceProvider gameServiceProvider;
    private GsonBuilder builder;
    private String lobbyId;
    private int maxRank;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //check Rating to match the game zone to join 
        if(session.action().equals("onPlay")){
            Rating rating = this.gameServiceProvider.rating(session.systemId());
            int mix = rating.rank>maxRank?maxRank:rating.rank;
            Descriptor lobby = mLobby.get(mix);
            //this.context.log("ACCESS MODE->"+session.accessMode(),OnLog.WARN);
            Response response = context.presence(session.systemId()).onPlay(session,lobby);
            if(response!=null) session.write(this.builder.create().toJson(response).getBytes());
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
        lobbyId = this.context.descriptor().typeId().replace("service","lobby");
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.maxRank = ((Number)this.gameServiceProvider.configuration().property("matchMakingMaxRank")).intValue();
        listLobby().addListener(this);
        context.log("Started match making module on ->"+this.context.descriptor().tag(), OnLog.WARN);
    }



    @Override
    public void onLobby(Descriptor descriptor) {
        this.context.log("Lobby Updated : disable["+descriptor.disabled()+"] rank["+descriptor.accessRank()+"]", OnLog.WARN);
        if(descriptor.accessRank()>0&&descriptor.accessRank()<=this.maxRank){
            mLobby.clear();
            listLobby();
        }
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
        if(start<maxRank){
            Descriptor lastZone = mLobby.get(start-1);
            for(int i = start;i<=maxRank;i++){
                mLobby.put(i,lastZone);
            }
        }
        //mLobby.forEach((k,v)->{
            //context.log("Access Rank ["+k+"] registered on ["+v.accessRank()+"]",OnLog.WARN);
        //});
        return lobby;
    }
}
