package com.tarantula.platform.leaderboard;

import com.tarantula.RecoverableFactory;

/**
 * Updated by yinghu lu on 8/24/19
 */
public class LeaderBoardEntryQuery implements RecoverableFactory<EntryImpl>{

    private  String leaderBoardId;

    public LeaderBoardEntryQuery(String leaderBoardId){
        this.leaderBoardId = leaderBoardId;
    }

    public EntryImpl create() {
        return new EntryImpl();
    }

    public int registryId() {
        return 0;
    }

    public String label(){
        return "E";
    }
    public String distributionKey(){
        return leaderBoardId;
    }

}
