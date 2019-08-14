package com.tarantula.game.casino.craps;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class DoNotComeLine extends CrapsLine {

    public DoNotComeLine(Craps craps){
        super(Craps.DO_NOT_COME_LINE,"Do Not Come line",1d,craps);
        this.subscript = 2;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        if(!craps.diceStop.puck.on){
            return OFF_WAGER;
        }
        return super.wager(systemId,wager,x,y,ix);
    }
    public double payout(){
        double _pay = 0;
        if(craps.diceStop.stop==7||craps.diceStop.stop==11){
            //pass line won
            super.clear();
        }
        else if(craps.diceStop.stop==2||craps.diceStop.stop==3||craps.diceStop.stop==12){
            //pass line lost
            _pay = super.payout();
            super.clear();
        }
        else if((this.craps.diceStop.stop==4||this.craps.diceStop.stop==5||this.craps.diceStop.stop==6||this.craps.diceStop.stop==8||this.craps.diceStop.stop==9||this.craps.diceStop.stop==10)){
            //move bet line to the place number lose line
            CrapsLine mline = this.craps.betLines.get(200+this.craps.diceStop.stop);
            this.wagerList.forEach((w)->{
                mline.wagerList.add(w);
                mline.wagered = true;
            });
            super.clear();
        }
        return _pay;
    }
}
