package com.tarantula.game.casino.craps;

import com.tarantula.game.casino.BetLine;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class Big6Line extends CrapsLine {

    public Big6Line(Craps craps){
        super(Craps.BIG_6,"Big 6 line",1.2d,craps);
        this.subscript = 11;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        if(!craps.diceStop.puck.on){
            return BetLine.OFF_WAGER;
        }
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        double _pay = 0;
        if(craps.diceStop.stop==7){
            //lost on 7
            super.clear();
        }
        else if(craps.diceStop.stop==6){
            //pass line won
            _pay = super.payout();
            super.clear();
        }
        return _pay;
    }
}
