package com.tarantula.game.service;

import com.icodesoftware.LeaderBoard;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Stub;
import com.tarantula.platform.leaderboard.LeaderBoardView;
import com.tarantula.platform.leaderboard.LeaderBoardViewSerializer;

import java.util.ArrayList;

public class RequestLeaderBoardCommand extends ServiceCommandHeader implements GameLobby.ServiceMessageListener {


    @Override
    public byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        String category  = messageBuffer.readUTF8();
        String classifier = messageBuffer.readUTF8();
        LeaderBoard ldb = gameServiceProvider.leaderBoard(category);
        LeaderBoardView view = new LeaderBoardView();
        view.category = ldb.category();
        view.classifier = classifier;
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
        LeaderBoardViewSerializer serializer = new LeaderBoardViewSerializer();
        return serializer.serialize(view,LeaderBoardView.class,null).toString().getBytes();
    }
}
