package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.RatingSerializer;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.service.Rating;
import com.tarantula.platform.leaderboard.LeaderBoardSerializer;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.statistics.StatisticsSerializer;

public class StatisticsModule implements Module {
    private ApplicationContext context;
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //fetch statistics from systemId
        if(session.action().equals("onRating")){
            Rating rating = this.gameServiceProvider.rating(session.systemId());
            session.write(this.builder.create().toJson(rating).getBytes(),label());
        }
        else if(session.action().equals("OnStatistics")){
            Statistics statistics = this.gameServiceProvider.statistics(session.systemId());
            session.write(this.builder.create().toJson(statistics).getBytes(),label());
        }
        else if(session.action().equals("OnLeaderBoard")){
            LeaderBoard ldb = this.gameServiceProvider.leaderBoard(session.systemId());
            session.write(this.builder.create().toJson(ldb).getBytes(),label());
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
        this.builder.registerTypeAdapter(LeaderBoard.class,new LeaderBoardSerializer());
        String gz = this.context.descriptor().typeId().replace("-statistics","-service");
        this.gameServiceProvider = this.context.serviceProvider(gz);
        this.context.log("Statistics started on game service provider ["+gz+"]", OnLog.WARN);
    }

    @Override
    public String label() {
        return "stats";
    }
}
