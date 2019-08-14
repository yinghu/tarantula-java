package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;

import java.time.LocalDateTime;

/**
 * Created by yinghu lu on 6/15/2018.
 */
public class DailyReset implements LeaderBoard.Reset {

    int updated;
    @Override
    public boolean reset(LeaderBoard leaderBoard) {
        boolean reset =false;
        if(updated==0){
            updated = LocalDateTime.now().getDayOfYear();
        }
        else{
            int today = LocalDateTime.now().getDayOfYear();
            reset = today!=updated;
            if(reset){
                updated = today;
            }
        }
        return reset;
    }
}
