package com.tarantula;

/**
 * Updated by yinghu on 6/10/2018.
 */
public interface OnLeaderBoard extends OnApplication {

    String TOTAL = "T";
    String DAILY = "D";
    String WEEKLY = "W";
    String MONTHLY = "M";
    String YEARLY = "Y";

    String leaderBoardHeader();
    Entry[] entryList();

    interface Entry extends Recoverable {

       String systemId();
       void systemId(String systemId);

       String header();
       void header(String header);


       String category();
       void category(String category);


       void value(String classifier,double value);
       double value(String classifier);


    }

}
