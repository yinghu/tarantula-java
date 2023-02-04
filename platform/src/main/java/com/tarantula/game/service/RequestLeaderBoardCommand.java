package com.tarantula.game.service;

import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.platform.leaderboard.LeaderBoardView;


import java.util.ArrayList;

public class RequestLeaderBoardCommand extends GameServiceProxyHeader {


    public RequestLeaderBoardCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }

    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
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
        return view.toJson().toString().getBytes();
    }
}
