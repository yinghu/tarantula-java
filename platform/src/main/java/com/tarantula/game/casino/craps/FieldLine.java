package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class FieldLine extends CrapsLine {

    public FieldLine(Craps craps){
        super(Craps.FIELD,"Field line",1d,craps);
        this.subscript = 10;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        double _pay = 0;//2, 12 pay 2x
        if(craps.diceStop.stop==3||craps.diceStop.stop==4||craps.diceStop.stop==9||craps.diceStop.stop==10||craps.diceStop.stop==11){
            _pay = super.payout();
            super.clear();
        }
        else if(craps.diceStop.stop==2||craps.diceStop.stop==12){
            odd = 2d;
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
