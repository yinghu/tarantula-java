package com.tarantula.platform.leaderboard;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.OnBalanceTrack;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.presence.SubscriptionFee;

/**
 * updated by yinghu lu on 5/1/2020.
 */
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
