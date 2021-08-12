package com.tarantula.game;


import java.util.Comparator;

public class GameZoneComparator implements Comparator<GameZone> {

    public int compare(GameZone o1, GameZone o2) {
        int diff = o1.levelMatch()-o2.levelMatch();
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
