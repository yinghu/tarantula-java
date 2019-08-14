package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

/**
 * Created by yinghu lu on 5/14/2018.
 */
public class LeaderBoardRegistryContext extends ResponseHeader{

    public List<LeaderBoard.Registry> registryList;
}
