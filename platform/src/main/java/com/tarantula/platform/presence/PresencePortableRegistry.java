package com.tarantula.platform.presence;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;
import com.tarantula.platform.OnBalanceTrack;
import com.tarantula.platform.PresenceIndex;

/**
 * Created by yinghu lu on 3/31/2018.
 */
public class PresencePortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 5;

    public static final int PRESENCE_CID = 1;
    public static final int ON_BALANCE_CID = 2;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case PRESENCE_CID:
                pt = new PresenceIndex();
                break;
            case ON_BALANCE_CID:
                pt = new OnBalanceTrack();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
