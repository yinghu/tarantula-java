package com.tarantula.platform.leaderboard;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.Metadata;
import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;


/**
 * Created by yinghu lu on 3/31/2018.
 */
public class LeaderBoardPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 11;

    public static final int LEADER_BOARD_ENTRY_CID = 2;
    public static final int ON_LEADER_BOARD_CID = 3;
    public static final int ON_LEADER_BOARD_ENTRY_CID = 4;

    public static final int TOP10_LEADER_BOARD_CID = 7;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case LEADER_BOARD_ENTRY_CID:
                pt = new LeaderBoardEntry();
                break;
            case ON_LEADER_BOARD_CID:
                pt = new OnLeaderBoardTrack();
                break;
            case ON_LEADER_BOARD_ENTRY_CID:
                pt = new OnLeaderBoardEntryTrack();
                break;
            case TOP10_LEADER_BOARD_CID:
                pt = new Top10LeaderBoard();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }

}
