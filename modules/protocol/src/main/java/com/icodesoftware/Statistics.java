package com.icodesoftware;

import java.util.List;


public interface Statistics extends Recoverable{


    Entry entry(String key);

    //heavy operation!
    List<Entry> summary();
    void summary(Stream query);

    void registerListener(Listener listener);

    interface Entry extends Recoverable, DataStore.Updatable{
        String name();
        double total();
        double hourly();
        double daily();
        double weekly();
        double monthly();
        double yearly();

        Entry update(double delta);
    }
    interface Stream{
        void onEntry(Entry entry);
    }
    interface Listener{
        void entryUpdated(Entry entry);
    }

}
