package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class DoNotPassLine extends CrapsLine {

    public DoNotPassLine(Craps craps){
        super(Craps.DO_NOT_PASS_LINE,"Do Not Pass line",1d,craps);
        this.subscript = 1;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        if(craps.diceStop.puck.on){
            return OFF_WAGER;
        }
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        if(craps.diceStop.puck.on){
            return 0;
        }
        double _pay = 0;
        if(craps.diceStop.puck.point==0&&(craps.diceStop.stop==7||craps.diceStop.stop==11)){
            //pass line won
            super.clear();
        }
        else if(craps.diceStop.puck.point==0&&(craps.diceStop.stop==2||craps.diceStop.stop==3||craps.diceStop.stop==12)){
            //pass line lost
            if(craps.diceStop.stop==12){
                odd = 0;
            }
            _pay = super.payout();
            super.clear();
        }
        else if(craps.diceStop.puck.point==craps.diceStop.stop){
            //pass line won
            super.clear();
        }
        else if(craps.diceStop.puck.point!=0&&craps.diceStop.stop==7){
            //pass lost
            _pay = super.payout();
            super.clear();
        }
        return _pay;
    }
}
