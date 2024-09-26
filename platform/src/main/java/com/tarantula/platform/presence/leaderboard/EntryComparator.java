package com.tarantula.platform.presence.leaderboard;

import com.icodesoftware.LeaderBoard;

import java.util.Comparator;


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
