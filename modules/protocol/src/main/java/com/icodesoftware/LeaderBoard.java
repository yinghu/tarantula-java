package com.icodesoftware;

public interface LeaderBoard extends JsonSerializable {

    String HOURLY = "hourly";
    String DAILY = "daily";
    String WEEKLY = "weekly";
    String MONTHLY = "monthly";
    String YEARLY = "yearly";
    String TOTAL = "total";

    int size();
    String category(); //The category of statistics eg WonCount, LostCount

    Board daily();
    Board weekly();
    Board monthly();
    Board yearly();
    Board total();

    void onAllBoard(Statistics.Entry entry);

    interface Board extends DataStore.Updatable,JsonSerializable{
        void onBoard(Entry update);
        default void rank(Listener ranking){}
    }

    interface Entry extends OnApplication, DataStore.Updatable{
        String category();
        String classifier();//board name daily, weekly, total
        double value();

        int rank();
        void rank(int rank);
        Entry update(Entry entry);
    }
    interface Listener{
        void onUpdated(Entry entry);
    }
}
