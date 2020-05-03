package com.tarantula;

import java.util.Map;

/**
 * Updated by yinghu on 5/2/2020
 */
public interface Statistics{


    Entry entry(String key);

    Map<String,Double> summary();

    interface Entry extends Recoverable,Updatable{
        String name();
        double total();
        double daily();
        double weekly();
        Entry update(double delta);
    }
}
