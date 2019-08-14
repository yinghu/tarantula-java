package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class Place8Line extends CrapsLine {

    public Place8Line(Craps craps){
        super(Craps.PLACE_8,"Place 8 line",1.2d,craps);
        this.subscript = 6;
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
        else if(craps.diceStop.stop==8){
            //pass line won
            _pay = super.payout();
            super.clear();
        }
        return _pay;
    }
}
