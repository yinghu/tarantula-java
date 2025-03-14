package com.tarantula.platform.presence.pvp;


import java.util.Comparator;

public class SeasonPlayerComparator implements Comparator<SeasonPlayerIndex> {

    public int compare(SeasonPlayerIndex o1, SeasonPlayerIndex o2) {
        int diff = o1.elo-o2.elo;
        if(diff>0){
            return -1;
        }
        else if(diff<0){
            return 1;
        }
        else{
            long tf = o1.timestamp()-o2.timestamp();
            return tf<=0?-1:1;
        }
    }
}
