package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class Hard10Line extends CrapsLine {

    public Hard10Line(Craps craps){
        super(Craps.HARD_10,"Hard 10 line",7d,craps);
        this.subscript = 18;
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
        else if(craps.diceStop.stop==10&&craps.diceStop.dice[0]==craps.diceStop.dice[1]){
            //pass line won
            _pay = super.payout();
            super.clear();
        }
        return _pay;
    }
}
