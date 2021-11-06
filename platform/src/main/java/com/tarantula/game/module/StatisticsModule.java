package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.RatingSerializer;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.leaderboard.LeaderBoardView;
import com.tarantula.platform.leaderboard.LeaderBoardViewSerializer;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import com.tarantula.platform.statistics.StatisticsSerializer;
import com.tarantula.platform.statistics.StatsDelta;

import java.util.ArrayList;

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
        this.builder.registerTypeAdapter(Rating.class,new RatingSerializer());
        this.builder.registerTypeAdapter(LeaderBoardView.class,new LeaderBoardViewSerializer());
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("Statistics started on game service provider ["+this.context.descriptor().typeId()+"]", OnLog.WARN);
    }

}
