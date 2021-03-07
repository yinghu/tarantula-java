package com.tarantula.platform.presence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.OnBalanceTrack;
import com.tarantula.platform.PresenceIndex;

/**
 * updated by yinghu lu on 5/1/2020.
 */
public class PresencePortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 3;

    public static final int PRESENCE_CID = 1;
    public static final int ON_BALANCE_CID = 2;

    public static final int GAME_CLUSTER_CID = PortableEventRegistry.GAME_CLUSTER_CID;
    public static final int PURCHASE_CID = 6;



    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case PRESENCE_CID:
                pt = new PresenceIndex();
                break;
            case ON_BALANCE_CID:
                pt = new OnBalanceTrack();
                break;
            case PURCHASE_CID:
                pt = new SubscriptionFee();
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
