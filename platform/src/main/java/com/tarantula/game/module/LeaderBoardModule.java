package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.leaderboard.LeaderBoardView;

import java.util.ArrayList;

public class LeaderBoardModule implements Module {
    private ApplicationContext context;
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {

        if(session.action().startsWith("onLeaderBoard")){ //use query OnLeaderBoard/{category}/{classifier} //onLeaderBoard/wins/total
            String[] query = session.action().split(Recoverable.PATH_SEPARATOR);
            if(query.length==3){
                LeaderBoard ldb = this.gameServiceProvider.leaderBoard(query[1]);
                LeaderBoardView view = new LeaderBoardView();
                view.category = ldb.category();
                view.classifier = query[2];//total/daily/weekly/monthly/yearly
                view.board = new ArrayList<>();
                int[] size  ={0};
                if(view.classifier.equals(LeaderBoard.DAILY)) {
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                else if(view.classifier.equals(LeaderBoard.WEEKLY)) {
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                else if(view.classifier.equals(LeaderBoard.MONTHLY)) {
                    ldb.total().rank((r, e) -> {
                        view.board.add(new LeaderBoardView.EntryView(r, e.owner(), e.value(), e.timestamp()));
                        size[0]++;
                    });
                }
                else if(view.classifier.equals(LeaderBoard.YEARLY)) {
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
                session.write(view.toJson().toString().getBytes());
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
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("Leader board module started ["+this.context.descriptor().typeId()+"]", OnLog.WARN);
    }

}
