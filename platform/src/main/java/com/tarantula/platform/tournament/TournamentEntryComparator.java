package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

import java.util.Comparator;

public class TournamentEntryComparator implements Comparator<Tournament.Entry> {

    public int compare(Tournament.Entry o1, Tournament.Entry o2) {
        double diff = o1.score(0)-o2.score(0);
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
