package com.tarantula;


import java.util.List;

/**
 * Updated by yinghu lu on 8/23/19
 */
public interface OnStatistics extends Recoverable{

    String name();

    double xpDelta();
    void xpDelta(double delta);

    List<Statistics.Entry> entryList();
    void onEntry(String name,double value);
}
