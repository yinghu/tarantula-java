package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.util.SystemUtil;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Created by yinghu lu on 6/15/2018.
 */
public class WeeklyReset  {


    public boolean reset(LeaderBoard leaderBoard) {
        boolean reset = LocalDateTime.now().getDayOfWeek().equals(DayOfWeek.SATURDAY);
        if(reset){
            leaderBoard.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
        }
        return reset;
    }

}
