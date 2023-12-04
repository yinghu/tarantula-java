package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.RecoverableObject;


public class TournamentScheduleStatus extends RecoverableObject {

    public Tournament.Status status = Tournament.Status.PENDING;//-> STARTING -> STARTED -> PENDING

    public long tournamentId;

    public TournamentScheduleStatus(long tournamentId){
        this();
        this.tournamentId = tournamentId;
    }
    public TournamentScheduleStatus(){
        this.onEdge = true;
        this.label = Tournament.SCHEDULE_LABEL;
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

    @Override
    public byte[] toBinary() {
        return BufferUtil.fromLong(tournamentId);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("TournamentId",tournamentId);
        json.addProperty("Status",status.name());
        json.addProperty("DistributionId",distributionKey());
        return json;
    }
}
