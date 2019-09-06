package com.tarantula;

import java.util.Map;

/**
 * Updated by yinghu on 8/23/2019.
 */
public interface Statistics extends OnApplication,Updatable{

    String leaderBoardHeader();

    OnStatistics value(String key,double value);

    Map<String,Double> summary();

    interface Entry extends Recoverable{
        String name();
        double value();
        void value(double value);
    }
}
