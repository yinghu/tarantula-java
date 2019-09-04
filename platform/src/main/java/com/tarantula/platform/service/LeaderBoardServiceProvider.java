package com.tarantula.platform.service;

import com.tarantula.LeaderBoard;

/**
 *  Updated by yinghu lu on 8/24/2019.
 */
public interface LeaderBoardServiceProvider extends ServiceProvider {

    //query leader board with {header}/{category}/{classifier} ex presence/LoginCount/daily
    LeaderBoard leaderBoard(String header, String category, String classifier);

    //commit leader board
    void onLeaderBoard(String systemId,LeaderBoard.Entry[] entries);
}
