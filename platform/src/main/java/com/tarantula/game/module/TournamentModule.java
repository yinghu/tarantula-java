package com.tarantula.game.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;

import java.util.concurrent.ConcurrentHashMap;

public class TournamentModule implements Module , Tournament.Listener {
    private ApplicationContext context;
    private TournamentServiceProvider tournamentServiceProvider;
    private String regKey;
    private ConcurrentHashMap<String,Tournament> tournaments = new ConcurrentHashMap<>();
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            session.write(toList().toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        GameServiceProvider gameServiceProvider = context.serviceProvider(context.descriptor().typeId());
        this.tournamentServiceProvider = gameServiceProvider.tournamentServiceProvider();
        regKey = this.tournamentServiceProvider.registerTournamentListener(this);
        this.context.log("tournament module started", OnLog.WARN);
    }
    @Override
    public void clear(){
        this.tournamentServiceProvider.unregisterTournamentListener(regKey);
    }
    public void tournamentStarted(Tournament tournament){
        this.context.log("tournament started->"+tournament.distributionKey(),OnLog.WARN);
        tournaments.put(tournament.distributionKey(),tournament);
    }
    public void tournamentClosed(Tournament tournament){
        this.context.log("tournament closed->"+tournament.distributionKey(),OnLog.WARN);

    }
    public void tournamentEnded(Tournament tournament){
        this.context.log("tournament ended->"+tournament.distributionKey(),OnLog.WARN);
    }
    private JsonObject toList(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray alist = new JsonArray();
        tournaments.forEach((k,v)->{
            JsonObject tt = new JsonObject();
            tt.addProperty("tournamentId",k);
            tt.addProperty("type",v.type());
            alist.add(tt);
        });
        jsonObject.add("tournamentList",alist);
        return jsonObject;
    }

}
