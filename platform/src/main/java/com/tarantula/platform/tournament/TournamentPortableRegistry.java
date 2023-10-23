package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.event.PortableEventRegistry;

public class TournamentPortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 4;


    public static final int TOURNAMENT_CID = PortableEventRegistry.TOURNAMENT_CID;
    public static final int TOURNAMENT_INSTANCE_CID = PortableEventRegistry.TOURNAMENT_INSTANCE_CID;
    public static final int TOURNAMENT_ENTRY_CID = PortableEventRegistry.TOURNAMENT_ENTRY_CID;
    public static final int TOURNAMENT_RACE_BOARD_CID = PortableEventRegistry.TOURNAMENT_RACE_BOARD_CID;

    public static final int TOURNAMENT_SCHEDULE_CID = 12;

    public static final int TOURNAMENT_PRIZE_CID = 15;
    public static final int TOURNAMENT_HISTORY_CID = 16;

    public static final int TOURNAMENT_SCHEDULE_STATUS_CID = 17;


    public static TournamentPortableRegistry INS;

    public TournamentPortableRegistry(){
        INS = this;
    }

    public T create(int i) {
        Recoverable pt = null;
        switch (i){
            case TOURNAMENT_CID:
                pt = new TournamentManager();
                break;
            case TOURNAMENT_INSTANCE_CID:
                pt = new TournamentInstance();
                break;
            case TOURNAMENT_ENTRY_CID:
                pt = new TournamentEntry();
                break;
            case TOURNAMENT_SCHEDULE_CID:
                pt = new TournamentSchedule();
                break;
            case TOURNAMENT_RACE_BOARD_CID:
                pt = new TournamentRaceBoard();
                break;
            case TOURNAMENT_PRIZE_CID:
                pt = new TournamentPrize();
                break;
            case TOURNAMENT_HISTORY_CID:
                pt = new TournamentHistory();
                break;
            case TOURNAMENT_SCHEDULE_STATUS_CID:
                pt = new TournamentScheduleStatus();
                break;

            default:
        }
        return (T)pt;
    }

    public int registryId() {
        return OID;
    }

}
