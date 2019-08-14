package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class PlaceNot6Line extends CrapsLine {

    public PlaceNot6Line(Craps craps){
        super(Craps.PLACE_NOT_6,"Place NOT 6 line",1.2d,craps);
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
            //WIN on 7
            _pay = super.payout();
            super.clear();
        }
        else if(craps.diceStop.stop==6){
            super.clear();
        }
        return _pay;
    }
}
