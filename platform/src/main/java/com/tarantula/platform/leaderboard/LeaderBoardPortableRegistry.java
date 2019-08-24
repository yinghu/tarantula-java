package com.tarantula.platform.leaderboard;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;


/**
 * Updated by yinghu lu on 8/24/2019.
 */
public class LeaderBoardPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 6;

    public static final int LEADER_BOARD_ENTRY_CID = 2;

    public static final int TOP10_LEADER_BOARD_CID = 7;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case LEADER_BOARD_ENTRY_CID:
                pt = new LeaderBoardEntry();
                break;
            case TOP10_LEADER_BOARD_CID:
                pt = new TopListLeaderBoard();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }

}
