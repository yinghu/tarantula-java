package com.tarantula;

import java.util.Map;

/**
 * Updated by yinghu on 11/23/2017.
 */
public interface Statistics extends OnApplication {


    Level level();
    void level(Level level);

    String leaderBoardHeader();
    void leaderBoardHeader(String header);

    double value(String key,double value);

    Map<String,Double> list();

    void entry(Entry entry);

    interface Entry extends Recoverable{
        String name();
        double value();
        void value(double value);
    }
}
