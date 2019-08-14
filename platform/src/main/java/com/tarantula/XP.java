package com.tarantula;

/**
 * Updated 4/23/2018 yinghu lu
 */
public interface XP extends Recoverable{


    void reset(String xName);

    String header();
    String category();
    void  header(String header);
    void category(String category);

    double dailyGain(double delta);
    double weeklyGain(double delta);
    double monthlyGain(double delta);
    double yearlyGain(double delta);
    double totalGain(double delta);
}
