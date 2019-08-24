package com.tarantula;



/**
 * Updated by yinghu on 6/14/2018.
 */
public interface LeaderBoard extends Recoverable {

    String TOTAL = "T";
    String DAILY = "D";
    String WEEKLY = "W";
    String MONTHLY = "M";
    String YEARLY = "Y";

    String name();
    void name(String name);

    int size();
    void size(int size);

    String leaderBoardHeader();
    void leaderBoardHeader(String header);

    String classifier();
    void classifier(String classifier);

    String category();
    void category(String category);


    void registerReset(Reset reset);

    void onBoard(String systemId,LeaderBoard.Entry entry);

    void reset();
    Entry[] list();


    interface Entry extends Recoverable{
        String header();
        String category();
        String classifier();

        String name();
        String systemId();
        void value(double value);
        double value();
        void update(String systemId,double replace);
    }

    interface Registry{

    }

    interface Reset{
        boolean reset(LeaderBoard leaderBoard);
    }

}
