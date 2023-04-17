package com.icodesoftware.game.mahjong;


import com.icodesoftware.game.mahjong.Tile;

import java.util.Comparator;

public class TitleComparator implements Comparator<Tile> {

    public int compare(Tile o1, Tile o2) {
        int diff = o1.sequence-o2.sequence;
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
