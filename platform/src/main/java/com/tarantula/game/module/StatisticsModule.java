package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;



public class StatisticsModule implements Module {
    private ApplicationContext context;

    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //fetch statistics from systemId
        if(session.action().equals("onStatistics")){
            Statistics statistics = this.gameServiceProvider.statistics(session.systemId());
            session.write(statistics.toJson().toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;

        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("Statistics started on game service provider ["+this.context.descriptor().typeId()+"]", OnLog.WARN);
    }

}
