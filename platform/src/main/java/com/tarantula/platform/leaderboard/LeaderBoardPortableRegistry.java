package com.tarantula.platform.leaderboard;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;


public class LeaderBoardPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 6;

    public static final int LEADER_BOARD_ENTRY_CID = 4;



    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case LEADER_BOARD_ENTRY_CID:
                pt = new LeaderBoardEntry();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
