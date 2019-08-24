package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;

import java.util.Comparator;

/**
 * Updated by yinghu on 8/24/19
 */
public class EntryComparator implements Comparator<LeaderBoard.Entry> {

    public int compare(LeaderBoard.Entry o1, LeaderBoard.Entry o2) {
        double diff = o1.value() - o2.value();
        if(diff>0){
            return -1;
        }
        else if(diff<0){
            return 1;
        }
        else{
            return o1.timestamp()>o2.timestamp()?-1:1;
        }
    }
}
