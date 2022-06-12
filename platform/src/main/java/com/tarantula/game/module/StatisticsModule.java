package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.statistics.StatisticsSerializer;


public class StatisticsModule implements Module {
    private ApplicationContext context;
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //fetch statistics from systemId
        if(session.action().equals("onStatistics")){
            Statistics statistics = this.gameServiceProvider.statistics(session.systemId());
            session.write(this.builder.create().toJson(statistics).getBytes());
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
        this.builder.registerTypeAdapter(StatisticsIndex.class,new StatisticsSerializer());
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("Statistics started on game service provider ["+this.context.descriptor().typeId()+"]", OnLog.WARN);
    }

}
