package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.DeltaStatistics;
import com.tarantula.platform.util.StatisticsSerializer;

public class StatisticsModule implements Module {
    private ApplicationContext context;
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //fetch statistics from systemId
        if(session.action().equals("OnStatistics")){
            Statistics statistics = this.gameServiceProvider.statistics(session.systemId());
            session.write(this.builder.create().toJson(statistics).getBytes(),label());
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
        this.builder.registerTypeAdapter(DeltaStatistics.class,new StatisticsSerializer());
        String gz = this.context.descriptor().typeId().replace("-statistics","-data-service");
        this.gameServiceProvider = this.context.serviceProvider(gz);
        this.context.log("Statistics started on game service provider ["+gz+"]", OnLog.WARN);
    }

    @Override
    public String label() {
        return "stats";
    }
}
