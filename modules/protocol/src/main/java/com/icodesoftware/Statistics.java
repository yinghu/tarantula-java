package com.icodesoftware;

import java.util.List;


public interface Statistics {


    Entry entry(String key);

    //heavy operation!
    List<Entry> summary();
    void summary(Stream query);

    interface Entry extends Recoverable, DataStore.Updatable{
        String name();
        double total();
        double daily();
        double weekly();
        double monthly();
        double yearly();
        Entry update(double delta);
    }
    interface Stream{
        void onEntry(Entry entry);
    }

}
