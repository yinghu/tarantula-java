package com.tarantula;

/**
 * Updated by yinghu on 4/30/20
 */
public interface LeaderBoard{

    int size();
    String category(); //The category of statistics eg WonCount, LostCount
    void addListener(Listener listener);
    void removeListener(Listener listener);

    Board daily();
    Board weekly();
    Board monthly();
    Board yearly();
    Board total();
    void onAllBoard(Statistics.Entry entry);
    interface  Board extends Updatable{
        void onBoard(String systemId,double value);
        default void rank(Ranking ranking){}
    }
    interface Ranking{
        boolean on(int rank,Entry entry);
    }
    interface Entry extends Recoverable,Updatable{
        String category();
        String classifier();//board name daily, weekly, total
        double value();
        Entry update(Entry entry);
    }
    interface Listener{
        void onUpdated(Entry entry);
    }
}
