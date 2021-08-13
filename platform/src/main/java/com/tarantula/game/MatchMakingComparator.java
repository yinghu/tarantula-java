package com.tarantula.game;


import com.icodesoftware.Descriptor;

import java.util.Comparator;

public class MatchMakingComparator implements Comparator<Descriptor> {

    public int compare(Descriptor o1, Descriptor o2) {
        int diff = o1.accessRank()-o2.accessRank();
        if(diff>0){
            return 1;
        }
        else if(diff<0){
            return -1;
        }
        else{
            return 0;
        }
    }
}
