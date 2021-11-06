package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.Rating;
import com.tarantula.game.RatingSerializer;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.leaderboard.LeaderBoardView;
import com.tarantula.platform.leaderboard.LeaderBoardViewSerializer;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import com.tarantula.platform.statistics.StatisticsSerializer;
import com.tarantula.platform.statistics.StatsDelta;

import java.util.ArrayList;

public class LeaderBoardModule implements Module {
    private ApplicationContext context;
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //fetch statistics from systemId
        if(session.action().startsWith("onLeaderBoard")){ //use query OnLeaderBoard/{category}/{classifier}
            String[] query = session.action().split(Recoverable.PATH_SEPARATOR);
            if(query.length==3){
                LeaderBoard ldb = this.gameServiceProvider.leaderBoard(query[1]);
                LeaderBoardView view = new LeaderBoardView();
                view.category = ldb.category();
                view.classifier = query[2];
                view.board = new ArrayList<>();
                int[] size  ={0};
                if(view.category.equals(LeaderBoard.DAILY)) {
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                else if(view.category.equals(LeaderBoard.WEEKLY)) {
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                else if(view.category.equals(LeaderBoard.MONTHLY)) {
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                else if(view.category.equals(LeaderBoard.YEARLY)) {
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                else{
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                view.size = size[0];
                session.write(this.builder.create().toJson(view).getBytes());
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
        this.context.log("Leader board module started ["+this.context.descriptor().typeId()+"]", OnLog.WARN);
    }

}
