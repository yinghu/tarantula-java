package com.tarantula.game.casino.roulette;

import com.tarantula.game.casino.BetLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinghu lu on 1/12/2019.
 */
public class WheelStop extends BetLine {
    public String color;

    public WheelStop(String number,String color,int index){
        this.symbol = number;
        this.color = color;
        this.subscript  = index;
        this.name = "stop";
        this.label = "roulette";
    }

    public List<RouletteLine> lineList = new ArrayList<>();

    public void payout(){
        lineList.forEach((line)->{
            line.payout();
        });
    }
    public void clear(){
        lineList.forEach((line)->{
            line.clear();
            line.wagered = false;
        });
    }
}
