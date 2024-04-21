package com.tarantula.platform.presence.leaderboard;

import com.icodesoftware.LeaderBoard;
import com.icodesoftware.service.ServiceProvider;

public interface DistributionLeaderBoardService extends ServiceProvider {
    String NAME = "DistributionLeaderBoardService";
    void onUpdateLeaderBoard(String serviceName, LeaderBoard.Entry leaderBoardEntry);

}
