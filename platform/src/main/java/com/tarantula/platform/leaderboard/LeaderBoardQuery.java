package com.tarantula.platform.leaderboard;

import com.tarantula.Recoverable;
import com.tarantula.RecoverableFactory;

/**
 * Updated by yinghu lu on 8/24/19
 */
public class LeaderBoardQuery implements RecoverableFactory<Top10LeaderBoard>{

    private  String bucket;

    public LeaderBoardQuery(String bucket){
        this.bucket = bucket;
    }

    public Top10LeaderBoard create() {
        return new Top10LeaderBoard();
    }

    public int registryId() {
        return 0;
    }

    public String label(){
        return "TLP";
    }
    public String distributionKey(){
        return this.bucket+ Recoverable.PATH_SEPARATOR+"TOP10";
    }

}
