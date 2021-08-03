package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.AbstractRecoverableListener;

public class TournamentPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 4;


    public static final int TOURNAMENT_CID = 8;
    public static final int TOURNAMENT_INSTANCE_CID = 9;
    public static final int TOURNAMENT_ENTRY_CID = 10;
    public static final int TOURNAMENT_SCHEDULE_CID = 12;

    public static final int TOURNAMENT_REGISTRY_CID = 13;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case TOURNAMENT_CID:
                pt = new TournamentHeader();
                break;
            case TOURNAMENT_INSTANCE_CID:
                pt = new TournamentInstanceHeader();
                break;
            case TOURNAMENT_ENTRY_CID:
                pt = new TournamentEntry();
                break;

            case TOURNAMENT_SCHEDULE_CID:
                pt = new DefaultTournamentSchedule();
                break;
            case TOURNAMENT_REGISTRY_CID:
                pt = new TournamentRegistry();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
    public <T extends Recoverable> RecoverableFactory<T> query(int registerId, String[] params){
        RecoverableFactory _fac = null;
        switch (registerId){
            case TOURNAMENT_ENTRY_CID:
                _fac = new TournamentEntryQuery(params[0]);
                break;
        }
        return _fac;
    }
}
