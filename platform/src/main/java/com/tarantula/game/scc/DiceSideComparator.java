package com.tarantula.game.scc;


import java.util.Comparator;

/**
 * Updated by yinghu lu on 5/6/2019
 */
public class DiceSideComparator implements Comparator<DiceSide> {

    public int compare(DiceSide o1, DiceSide o2) {
        int diff = o1.rank-o2.rank;
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
