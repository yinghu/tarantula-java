package com.tarantula.platform.playlist;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;
import com.tarantula.platform.OnSessionTrack;

/**
 * Created by yinghu lu on 3/31/2018.
 */
public class SessionPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 7;

    public static final int ON_SESSION_CID = 2;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case ON_SESSION_CID:
                pt = new OnSessionTrack();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }

}
