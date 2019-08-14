package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class Place6Line extends CrapsLine {

    public Place6Line(Craps craps){
        super(Craps.PLACE_6,"Place 6 line",1.2d,craps);
        this.subscript = 5;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        if(!craps.diceStop.puck.on){
            return OFF_WAGER;
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
