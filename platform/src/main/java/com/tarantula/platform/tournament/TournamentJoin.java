package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

public class TournamentJoin extends RecoverableObject{

    public static final String TOURNAMENT_JOIN_LABEL = "tournament_join";
    public static final String PLAYER_JOIN_LABEL = "player_tournament_join";

    public long systemId;
    public long stub;
    public long scheduleId;

    public int slot;
    public long instanceId;
    public boolean closed;
    public TournamentJoin(long stub,long scheduleId){
        this();
        this.stub = stub;
        this.scheduleId =  scheduleId;
    }
    public TournamentJoin(){
        this.onEdge = true;
        this.label = PLAYER_JOIN_LABEL;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(scheduleId);
        buffer.writeInt(slot);
        buffer.writeLong(instanceId);
        buffer.writeLong(stub);
        buffer.writeBoolean(closed);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        scheduleId = buffer.readLong();
        slot = buffer.readInt();
        instanceId = buffer.readLong();
        stub = buffer.readLong();
        closed = buffer.readBoolean();
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
        jsonObject.addProperty("scheduleId",Long.toString(scheduleId));
        jsonObject.addProperty("slot",slot);
        jsonObject.addProperty("instanceId",Long.toString(instanceId));
        jsonObject.addProperty("stub",Long.toString(stub));
        jsonObject.addProperty("closed",closed);
        return jsonObject;
    }

    void onTournament(long tournamentId){
        ownerKey = SnowflakeKey.from(tournamentId);
        this.dataStore.createEdge(this,TOURNAMENT_JOIN_LABEL);
        closed = false;
        this.dataStore.update(this);
    }
    void onTournament(long tournamentId,int slot,long instanceId){
        ownerKey = SnowflakeKey.from(tournamentId);
        this.dataStore.createEdge(this,TOURNAMENT_JOIN_LABEL);
        this.closed = false;
        this.slot = slot;
        this.instanceId = instanceId;
        this.dataStore.update(this);
    }

    public static TournamentJoin init(Session session,long scheduleId){
        TournamentJoin tournamentJoin = new TournamentJoin(session.stub(),scheduleId);
        tournamentJoin.closed = true;
        tournamentJoin.ownerKey(SnowflakeKey.from(session.distributionId()));
        return tournamentJoin;
    }
}
