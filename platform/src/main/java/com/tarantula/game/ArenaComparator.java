package com.tarantula.game;

import java.util.Comparator;

/**
 * Updated by yinghu lu on 10/9/2018.
 */
public class ArenaComparator implements Comparator<Arena> {

    public int compare(Arena o1, Arena o2) {
        int diff = o1.level-o2.level;
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
