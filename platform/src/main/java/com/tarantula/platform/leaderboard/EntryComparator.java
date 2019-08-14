package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;

import java.util.Comparator;

/**
 * Updated by yinghu on 6/15/2018.
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
            return 0;
        }
    }
}
