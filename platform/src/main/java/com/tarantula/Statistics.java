package com.tarantula;

import java.util.List;

/**
 * Updated by yinghu on 5/2/2020
 */
public interface Statistics{


    Entry entry(String key);

    //heavy operation!
    List<Entry> summary();

    interface Entry extends Recoverable,Updatable{
        String name();
        double total();
        double daily();
        double weekly();
        double monthly();
        double yearly();
        Entry update(double delta);
    }
}
