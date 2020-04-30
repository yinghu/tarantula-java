package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;

/**
 * Created by yinghu lu on 6/15/2018.
 */
public class MonthlyReset {


    public boolean reset(LeaderBoard leaderBoard) {
        boolean reset =  LocalDateTime.now().getDayOfMonth()==1;
        if(reset){
            leaderBoard.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
        }
        return reset;
    }

}
