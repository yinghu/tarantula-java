package com.tarantula.platform.presence;

import com.icodesoftware.LeaderBoard;
import com.icodesoftware.service.ServiceProvider;

public interface DistributionPresenceService extends ServiceProvider {

    String NAME = "DistributionPresenceService";

    int profileSequence(String serviceName,String name);

    void onUpdateLeaderBoard(String serviceName, LeaderBoard.Entry leaderBoardEntry);
    byte[] onLeaderBoard(String serviceName,String category,String classifier);

    boolean deleteUserLoginData(long playerID);

}
