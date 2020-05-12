package com.tarantula.platform.presence;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;
import com.tarantula.platform.OnAccessTrack;

/**
 * Updated by yinghu lu on 9/6/2019.
 */
public class UserPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 3;

    public static final int USER_CID = 1;

    public static final int USER_ACCOUNT_CID=2;

    public static final int ON_ACCESS_CID = 5;


    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case USER_CID:
                pt = new User();
                break;
            case USER_ACCOUNT_CID:
                pt = new UserAccount();
                break;
            case ON_ACCESS_CID:
                pt = new OnAccessTrack();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
