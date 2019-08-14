package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class PlaceNot5Line extends CrapsLine {

    public PlaceNot5Line(Craps craps){
        super(Craps.PLACE_NOT_5,"Place NOT 5 line",1.5d,craps);
        this.subscript = 4;
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
            //WIN on 7
            _pay = super.payout();
            super.clear();
        }
        else if(craps.diceStop.stop==5){
            super.clear();
        }
        return _pay;
    }
}
