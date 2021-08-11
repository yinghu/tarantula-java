package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.util.ResponseSerializer;
import java.util.concurrent.ConcurrentHashMap;


public class MatchMakingModule implements Module, Lobby.Listener {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Descriptor> mZone;
    private GameServiceProvider gameServiceProvider;
    private GsonBuilder builder;
    private String lobbyId;
    private int maxRank;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //check Rating to match the game zone to join 
        if(session.action().equals("onPlay")){
            Rating rating = this.gameServiceProvider.rating(session.systemId());
            int mix = rating.rank>maxRank?maxRank:rating.rank;
            Descriptor lobby = mZone.get(mix);
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
        mZone = new ConcurrentHashMap<>();//max matching level
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
            mZone.clear();
            listLobby();
        }
    }
    private Lobby listLobby(){
        Lobby lobby = this.context.lobby(lobbyId);
        int[] fi = {maxRank};
        lobby.entryList().forEach((a)->{
            if(a.accessRank()>0&&a.accessRank()<=maxRank){
                mZone.put(a.accessRank(),a);
                if(a.accessRank()<fi[0]){
                    fi[0] = a.accessRank();
                }
            }
        });
        //set 1 to max lobby count from capacity setting
        for(int i=1;i<maxRank+1;i++){//max matching level
            Descriptor ex = mZone.get(i);
            if(ex==null){
                if(mZone.get(i-1)!=null){
                    mZone.put(i,mZone.get(i-1));//fill with last one
                }
                else{
                    mZone.put(i,mZone.get(fi[0]));//fill header
                }
            }
            //context.log("Add lobby ->"+mZone.get(i).tag()+" ->rank ["+mZone.get(i).accessRank()+"]",OnLog.WARN);
        }
        return lobby;
    }
}
