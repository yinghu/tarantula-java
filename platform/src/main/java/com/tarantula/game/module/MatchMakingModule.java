package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.service.Rating;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class MatchMakingModule implements Module {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Descriptor> mZone = new ConcurrentHashMap<>();
    private GameServiceProvider gameServiceProvider;
    private GsonBuilder builder;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //check Rating to match the game zone to join 
        if(session.action().equals("onPlay")){
            Rating rating = this.gameServiceProvider.rating(session.systemId());
            Response response = context.presence(session.systemId()).onPlay(session,mZone.get(rating.rank));
            if(response==null){
                Statistics statistics = gameServiceProvider.statistics(session.systemId());
                statistics.entry("wc").value(1);
                statistics.update();
                LeaderBoard leaderBoard = gameServiceProvider.leaderBoard("wc");
                leaderBoard.onBoard(session.systemId(),statistics.entry("wc").value());
            }
            else{
                session.write(this.builder.create().toJson(response).getBytes(),label());
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
        String lb = this.context.descriptor().typeId().replace("service","lobby");
        Lobby lobby = this.context.lobby(lb);
        lobby.entryList().forEach((d)->{
            context.log("Add lobby ->"+d.tag()+" ->rank ["+d.accessRank()+"]",OnLog.WARN);
            mZone.put(d.accessRank(),d);
        });
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        context.log("Started match making module on ->"+this.context.descriptor().typeId(), OnLog.WARN);
    }

    @Override
    public String label() {
        return "matchmaking";
    }
}
