package com.tarantula.platform.leaderboard;

import com.tarantula.RecoverableFactory;

/**
 * Updated by yinghu lu on 9/26/2018.
 */
public class LeaderBoardEntryQuery implements RecoverableFactory<LeaderBoardEntry>{

    private  String leaderBoardId;

    public LeaderBoardEntryQuery(String leaderBoardId){
        this.leaderBoardId = leaderBoardId;
    }

    public LeaderBoardEntry create() {
        return new LeaderBoardEntry();
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
    public boolean onEdge(){
        return true;
    }
}
