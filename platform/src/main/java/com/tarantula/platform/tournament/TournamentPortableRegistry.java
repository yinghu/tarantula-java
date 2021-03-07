package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;

/**
 * updated by yinghu lu on 5/1/2020.
 */
public class TournamentPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 4;


    public static final int TOURNAMENT_CID = 8;
    public static final int TOURNAMENT_INSTANCE_CID = 9;
    public static final int TOURNAMENT_ENTRY_CID = 10;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case TOURNAMENT_CID:
                pt = new DefaultTournament();
                break;
            case TOURNAMENT_INSTANCE_CID:
                pt = new TournamentInstance();
                break;
            case TOURNAMENT_ENTRY_CID:
                pt = new TournamentEntry();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
