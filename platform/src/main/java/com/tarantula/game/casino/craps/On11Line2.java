package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class On11Line2 extends CrapsLine {

    public On11Line2(Craps craps){
        super(Craps.ON_11_B,"On 11 line",15d,craps);
        this.subscript = 23;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        double _pay = 0;//2, 12 pay 2x
        if(craps.diceStop.stop==11){
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
