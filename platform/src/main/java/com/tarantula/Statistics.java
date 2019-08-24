package com.tarantula;

import java.util.Map;

/**
 * Updated by yinghu on 8/23/2019.
 */
public interface Statistics extends OnApplication {

    String leaderBoardHeader();
    void leaderBoardHeader(String header);

    OnStatistics value(String key,double value);

    Map<String,Double> list();

    interface Entry extends Recoverable{
        String name();
        double value();
        void value(double value);
    }
}
