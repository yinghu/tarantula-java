package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class On12Line extends CrapsLine {

    public On12Line(Craps craps){
        super(Craps.ON_12,"On 12 line",30d,craps);
        this.subscript = 21;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        double _pay = 0;//2, 12 pay 2x
        if(craps.diceStop.stop==12){
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
