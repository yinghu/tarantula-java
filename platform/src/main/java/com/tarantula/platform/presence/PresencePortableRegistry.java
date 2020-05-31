package com.tarantula.platform.presence;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.OnBalanceTrack;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.leaderboard.LeaderBoardEntry;
import com.tarantula.platform.statistics.StatisticsEntry;

/**
 * updated by yinghu lu on 5/1/2020.
 */
public class PresencePortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 5;

    public static final int PRESENCE_CID = 1;
    public static final int ON_BALANCE_CID = 2;
    public static final int STATISTICS_CID = 3;
    public static final int LEADER_BOARD_ENTRY_CID = 4;
    public static final int STATISTICS_ENTRY_CID = 5;
    public static final int GAME_CLUSTER_CID = PortableEventRegistry.GAME_CLUSTER_CID;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case PRESENCE_CID:
                pt = new PresenceIndex();
                break;
            case ON_BALANCE_CID:
                pt = new OnBalanceTrack();
                break;
            case STATISTICS_CID:
                pt = new StatisticsIndex();
                break;
            case LEADER_BOARD_ENTRY_CID:
                pt = new LeaderBoardEntry();
                break;
            case STATISTICS_ENTRY_CID:
                pt = new StatisticsEntry();
                break;
            case GAME_CLUSTER_CID:
                pt = new GameCluster();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
