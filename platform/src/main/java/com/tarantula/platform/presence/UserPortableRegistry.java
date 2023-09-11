package com.tarantula.platform.presence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.OnAccessTrack;

public class UserPortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 2;

    public static final int USER_CID = 1;

    public static final int USER_ACCOUNT_CID=2;

    public static final int MEMBERSHIP_CID =3;

    public static final int ON_ACCESS_CID = 5;

    public UserPortableRegistry INS;

    public UserPortableRegistry(){
        INS = this;
    }

    public T create(int i) {
        Recoverable pt = null;
        switch (i){
            case USER_CID:
                pt = new User();
                break;
            case USER_ACCOUNT_CID:
                pt = new UserAccount();
                break;
            case MEMBERSHIP_CID:
                pt = new Membership();
                break;
            case ON_ACCESS_CID:
                pt = new OnAccessTrack();
                break;
            default:
        }
        return (T)pt;
    }

    public int registryId() {
        return OID;
    }
}
