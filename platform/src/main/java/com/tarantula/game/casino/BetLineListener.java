package com.tarantula.game.casino;

/**
 * Created by yinghu lu on 12/13/2018.
 */
public interface BetLineListener {
    boolean onWager(String systemId,double wager);
    boolean onPayout(String systemId,double payout);
    void onStatistics(String key,double value,int index);
    double balance(String systemId);
    double onHouse(double delta);
    
}
