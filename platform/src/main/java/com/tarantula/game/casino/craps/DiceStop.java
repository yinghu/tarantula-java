package com.tarantula.game.casino.craps;

import com.tarantula.game.casino.BetLine;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class DiceStop extends BetLine {

    public int stop; //2 ,3 , 4, 5, 6, 7, 8, 9, 10, 11, 12
    public final Craps craps;
    public int[] dice = new int[]{3,4};
    public Puck puck;

    public DiceStop(int stop,Craps craps){
        this.stop = stop;
        this.craps = craps;
        this.name = "stop";
        this.label = "craps";
    }

    public void stop(int d1,int d2){
        this.stop = d1+d2;
        dice[0]=d1;
        dice[1]=d2;
        if((!this.puck.on)&&(this.stop==4||this.stop==5||this.stop==6||this.stop==8||this.stop==9||this.stop==10)){
            //create the come-out point
            this.puck.on = true;
            this.puck.point = this.stop;
        }
        else if((!this.puck.on)&&(this.stop==2||this.stop==3||this.stop==12)){
            //craps do not pass line win round end
            this.craps.betLineListener.onStatistics("totalNotPassLine",1,1);
        }
        else if(!this.puck.on&&this.stop==7){
            //7 pass line win round end
            this.craps.betLineListener.onStatistics("totalPassLine",1,0);
        }
        else if((this.puck.on)&&(this.stop==this.puck.point)){
            //pass line win round end
            this.puck.on = false;
        }
        else if(this.puck.on&&this.stop==7){
            //do not pass line win round end
            this.puck.on = false;
        }
    }


}
