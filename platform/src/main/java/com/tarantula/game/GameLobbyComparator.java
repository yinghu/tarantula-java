package com.tarantula.game;

import java.util.Comparator;

/**
 * Created by yinghu lu on 4/29/2020.
 */
public class GameLobbyComparator implements Comparator<GameLobby> {

    public int compare( GameLobby o1, GameLobby o2) {
        int diff = o1.lobby.accessRank()-o2.lobby.accessRank();
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
