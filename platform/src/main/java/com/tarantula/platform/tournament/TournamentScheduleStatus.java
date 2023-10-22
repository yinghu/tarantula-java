package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;


public class TournamentScheduleStatus extends RecoverableObject {

    public final static String TOURNAMENT_SCHEDULE_LOOKUP_INDEX = "schedule";
    public Tournament.Status status = Tournament.Status.PENDING;//-> STARTING -> STARTED -> PENDING

    public long tournamentId;

    public TournamentScheduleStatus(){
        this.onEdge = true;
        this.label = TOURNAMENT_SCHEDULE_LOOKUP_INDEX;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(tournamentId);
        buffer.writeInt(status.ordinal());
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        tournamentId = buffer.readLong();
        status = Tournament.Status.values()[buffer.readInt()];
        return true;
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_STATUS_CID;
    }

    public String toString(){
        return "Tournament Schedule ["+tournamentId+"]["+status+"]";
    }

}
