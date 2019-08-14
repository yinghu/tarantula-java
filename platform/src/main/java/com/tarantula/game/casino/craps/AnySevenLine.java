package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class AnySevenLine extends CrapsLine {

    public AnySevenLine(Craps craps){
        super(Craps.ANY_SEVEN,"Any Seven line",4d,craps);
        this.subscript = 14;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        double _pay = 0;
        if(craps.diceStop.stop==7){
            _pay = super.payout();
            super.clear();
        }
        else{
            //pass line won
            super.clear();
        }
        return _pay;
    }
}
