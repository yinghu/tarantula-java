package com.tarantula;


/**
 * Updated by yinghu lu on 7/30/2018.
 */
public interface OnStatistics extends Recoverable{

    String name();

    double xpDelta();
    void xpDelta(double delta);

    Statistics.Entry[] entryList();
    void entryList(Statistics.Entry[] entryList);
}
