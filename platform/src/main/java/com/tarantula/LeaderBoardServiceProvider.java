package com.tarantula;

import java.util.List;

/**
 *  Updated by yinghu lu on 8/24/2019.
 */
public interface LeaderBoardServiceProvider extends ServiceProvider {


    LeaderBoard leaderBoard(String header,String category,String classifier);

    ///List<LeaderBoard.Registry> onRegistry();

    void onLeaderBoard(String systemId,LeaderBoard.Entry[] entries);
}
