package com.tarantula.game.casino;

import com.tarantula.game.CheckPoint;
/**
 * Updated by yinghu lu on 4/19/2019.
 */
public class BetLine extends CheckPoint {

    public static final int ON_WAGER = 0;
    public static final int OFF_WAGER = 1;
    public static final int NO_FUND = 2;

    public float x;
    public float y;

    public double odd;
    public String symbol;

    public BetLine(){
        this.name = "betline";
    }
    public BetLine(int betId,float x,float y){
        this();
        this.stub = betId;
        this.x = x;
        this.y = y;
    }
    public BetLine(int betId,float x,float y,int index,String systemId,double wager){
        this();
        this.stub = betId;
        this.x = x;
        this.y = y;
        this.subscript = index;
        this.systemId = systemId;
        this.entryCost = wager;
    }
    public double wager(){
        return entryCost;
    }
    public void  wager(double wager){
        this.entryCost = wager;
    }
}
