package com.tarantula;

/**
 * Updated 8/24/2019 yinghu lu
 */
public interface XP extends Recoverable{


    void reset(String xName);

    String header();
    String category();
    void  header(String header);
    void category(String category);

    LeaderBoard.Entry dailyGain(double delta);
    LeaderBoard.Entry weeklyGain(double delta);
    LeaderBoard.Entry monthlyGain(double delta);
    LeaderBoard.Entry yearlyGain(double delta);
    LeaderBoard.Entry totalGain(double delta);
}
