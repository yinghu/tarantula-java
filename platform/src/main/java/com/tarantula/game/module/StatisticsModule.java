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
        else if(session.action().startsWith("OnLeaderBoard")){ //use query OnLeaderBoard/{category}/{classifier}
            String[] query = session.action().split(Recoverable.PATH_SEPARATOR);
            if(query.length==3){
                LeaderBoard ldb = this.gameServiceProvider.leaderBoard(query[1]);
                LeaderBoardView view = new LeaderBoardView();
                view.category = ldb.category();
                view.classifier = query[2];
                view.board = new ArrayList<>();
                ldb.total().rank((r,e)->{
                    view.board.add(new LeaderBoardView.EntryView(r,e.owner(),e.value(),e.timestamp()));
                });
                session.write(this.builder.create().toJson(view).getBytes(),label());
            }
            else{
                throw new UnsupportedOperationException(session.action());
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
        this.builder.registerTypeAdapter(StatisticsIndex.class,new StatisticsSerializer());
        this.builder.registerTypeAdapter(Rating.class,new RatingSerializer());
        this.builder.registerTypeAdapter(LeaderBoardView.class,new LeaderBoardViewSerializer());
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.gameServiceProvider.statisticsTag(this.context.descriptor().tag());
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(StatisticsPortableRegistry.STATISTICS_DELTA_CID,(a)->{
            StatsDelta delta = (StatsDelta)a;
            Statistics statistics = gameServiceProvider.statistics(delta.owner());
            Statistics.Entry entry = statistics.entry(delta.name);
            entry.update(delta.value).update();
            LeaderBoard ldb = gameServiceProvider.leaderBoard(delta.name);
            ldb.onAllBoard(entry);
            //this.context.log("delta->"+delta.name+"<>"+delta.value+"//"+delta.owner(), OnLog.WARN);
        });
        this.context.registerRecoverableListener(new GamePortableRegistry()).addRecoverableFilter(GamePortableRegistry.STUB_CID,(a)->{
            Stub stub = (Stub)a;
            Rating rating = this.gameServiceProvider.rating(stub.owner());
            rating.update(stub);
            rating.update();
            //this.context.log("a->"+stub.owner()+"/"+stub.rank+"/"+stub.pxp+"/"+stub.rankUpBase+"/"+stub.levelUpBase,OnLog.WARN);
        });
        this.context.log("Statistics started on game service provider ["+this.context.descriptor().typeId()+"]", OnLog.WARN);
    }

    @Override
    public String label() {
        return this.context.descriptor().typeId();
    }
}
