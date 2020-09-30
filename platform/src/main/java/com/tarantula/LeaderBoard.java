package com.tarantula;

import com.icodesoftware.Recoverable;

import javax.xml.crypto.Data;

/**
 * Updated by yinghu on 4/30/20
 */
public interface LeaderBoard{

    String DAILY = "daily";
    String WEEKLY = "weekly";
    String MONTHLY = "monthly";
    String YEARLY = "yearly";
    String TOTAL = "total";

    int size();
    String category(); //The category of statistics eg WonCount, LostCount
    void addListener(Listener listener);

    Board daily();
    Board weekly();
    Board monthly();
    Board yearly();
    Board total();

    void onAllBoard(Statistics.Entry entry);
    interface  Board extends DataStore.Updatable{
        void onBoard(String systemId,double value);
        default void rank(Stream ranking){}
    }
    interface Stream{
        void onRank(int rank,Entry entry);
    }
    interface Entry extends Recoverable, DataStore.Updatable{
        String category();
        String classifier();//board name daily, weekly, total
        double value();
        Entry update(Entry entry);
    }
    interface Listener{
        void onUpdated(Entry entry);
    }
}
