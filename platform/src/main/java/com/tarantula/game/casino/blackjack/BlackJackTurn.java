package com.tarantula.game.casino.blackjack;

import com.tarantula.game.CheckPoint;

/**
 * Created by yinghu lu on 12/24/2018.
 */
public class BlackJackTurn extends CheckPoint {

    public int seat;
    protected BlackJack blackJack;

    public boolean onTurn(String systemId){
        if(this.systemId!=null&&this.systemId.equals(systemId)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void reset() {

    }
}
