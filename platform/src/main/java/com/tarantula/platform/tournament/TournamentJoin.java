package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class TournamentJoin extends RecoverableObject{

    public static final String TOURNAMENT_JOIN_LABEL = "tournament_join";
    public long stub;
    public long tournamentId;
    public TournamentJoin(long systemId,long stub,long tournamentId){
        this();
        this.distributionId = systemId;
        this.stub = stub;
        this.tournamentId = tournamentId;
    }
    public TournamentJoin(){
        this.onEdge = true;
        this.label = TOURNAMENT_JOIN_LABEL;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(tournamentId);
        buffer.writeLong(stub);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        tournamentId = buffer.readLong();
        stub = buffer.readLong();
        return true;
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_JOIN_CID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("tournamentId",Long.toString(tournamentId));
        jsonObject.addProperty("stub",Long.toString(stub));
        return jsonObject;
    }
}
