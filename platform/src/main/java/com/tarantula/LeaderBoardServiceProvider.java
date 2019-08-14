package com.tarantula;

import java.util.List;

/**
 * Created by yinghu lu on 6/14/2018.
 */
public interface LeaderBoardServiceProvider extends ServiceProvider {

    String NAME = "LeaderBoardServiceProvider";

    LeaderBoard leaderBoard(String header,String category,String classifier);

    void onLeaderBoard(OnLeaderBoard onLeaderBoard);

    List<LeaderBoard.Registry> onRegistry();
}
