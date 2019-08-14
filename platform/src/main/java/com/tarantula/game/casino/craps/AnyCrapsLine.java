package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class AnyCrapsLine extends CrapsLine {

    public AnyCrapsLine(Craps craps){
        super(Craps.ANY_CRAPS,"Any craps line",7d,craps);
        this.subscript = 24;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        double _pay = 0;
        if(craps.diceStop.stop==2||craps.diceStop.stop==3||craps.diceStop.stop==12){
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
