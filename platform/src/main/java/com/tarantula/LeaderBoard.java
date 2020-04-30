package com.tarantula;

/**
 * Updated by yinghu on 4/30/20
 */
public interface LeaderBoard extends Recoverable{

    String TOTAL = "T";
    String DAILY = "D";
    String WEEKLY = "W";
    String MONTHLY = "M";
    String YEARLY = "Y";

    int size();
    String name(); //Board name eg top10, top100
    String header();//The statistics group name eg a game name blackjack
    String category(); //The category of statistics eg WonCount, LostCount
    String classifier(); // ONE OF TOTAL, DAILY, WEEKLY, MONTHLY, YEARLY

    boolean onBoard(String systemId,LeaderBoard.Entry entry);

    void reset();
    Entry[] onBoard();


    interface Entry extends Recoverable{

        String systemId();
        double value();
        void update(String systemId,double replace,long timestamp);
    }
    interface Reset{
        boolean reset();
    }
}
