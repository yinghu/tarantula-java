package com.tarantula.game;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;


public class GamePortableRegistry  extends AbstractRecoverableListener {

    public static final int OID = 10;

    public static final int RATING_CID = 1;
    public static final int ARENA_CID = 2;
    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case RATING_CID:
                pt = new Rating();
                break;
            case ARENA_CID:
                pt = new Arena();
                break;
            default:
        }
        return pt;
    }
}
