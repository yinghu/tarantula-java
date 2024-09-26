package com.tarantula.platform.presence.leaderboard;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class LeaderBoardEntryQuery implements RecoverableFactory<LeaderBoardEntry> {

    private long boardId;
    private String classifier;
    private String category;

    public LeaderBoardEntryQuery(long boardId, String classifier,String category){
        this.boardId = boardId;
        this.classifier = classifier;
        this.category = category;
    }

    @Override
    public LeaderBoardEntry create() {
        return new LeaderBoardEntry(classifier,category);
    }

    @Override
    public String label() {
        return classifier+"_"+category;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(boardId);
    }
}
