package com.tarantula.platform.presence;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;

/**
 * Created by yinghu lu on 3/31/2018.
 */
public class UserPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 3;

    public static final int ACCESS_CID = 1;
    public static final int PROFILE_CID = 2;
    public static final int CONTENT_TRANSACTION_CID = 3;
    public static final int CONTENT_CHUNK_CID = 4;


    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case ACCESS_CID:
                pt = new AccessTrack();
                break;
            case PROFILE_CID:
                pt = new ProfileTrack();
                break;
            case CONTENT_TRANSACTION_CID:
                pt = new ContentTransaction();
                break;
            case CONTENT_CHUNK_CID:
                pt = new ContentChunk();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
