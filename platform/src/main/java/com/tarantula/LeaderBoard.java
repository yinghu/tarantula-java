package com.tarantula;

/**
 * Updated by yinghu on 8/24/19
 */
public interface LeaderBoard extends Response {

    String TOTAL = "T";
    String DAILY = "D";
    String WEEKLY = "W";
    String MONTHLY = "M";
    String YEARLY = "Y";

    String name(); //Board name eg top10, top100
    int size();
    String header();//The statistics group name eg a game name blackjack
    String category(); //The category of statistics eg WonCount, LostCount
    String classifier(); // ONE OF TOTAL, DAILY, WEEKLY, MONTHLY, YEARLY

    void registerReset(Reset reset);

    boolean onBoard(String systemId,LeaderBoard.Entry entry);

    void reset();
    Entry[] onBoard();


    interface Entry extends Recoverable{
        String header();
        String category();
        String classifier();
        String systemId();
        double value();
        void update(String systemId,double replace,long timestamp);
    }

    interface Reset{
        boolean reset(LeaderBoard leaderBoard);
    }

}
