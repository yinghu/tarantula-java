package com.tarantula.platform.presence;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;
import com.tarantula.platform.OnAccessTrack;

/**
 * Updated by yinghu lu on 9/6/2019.
 */
public class UserPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 3;

    public static final int ACCESS_CID = 1;
    public static final int PROFILE_CID = 2;
    public static final int AVATAR_CID = 3;
    public static final int ON_ACCESS_CID = 5;


    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case ACCESS_CID:
                pt = new AccessTrack();
                break;
            case PROFILE_CID:
                pt = new ProfileTrack();
                break;
            case ON_ACCESS_CID:
                pt = new OnAccessTrack();
                break;
            case AVATAR_CID:
                pt = new Avatar();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
