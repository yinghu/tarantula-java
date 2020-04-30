package com.tarantula;

import java.util.Map;

/**
 * Updated by yinghu on 8/23/2019.
 */
public interface Statistics extends Recoverable,Updatable{


    Entry entry(String key);

    Map<String,Double> summary();

    interface Entry{
        String name();
        double value();
        void value(double value);
    }
}
