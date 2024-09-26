package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.OnApplicationHeader;

public class TournamentJoin extends OnApplicationHeader {

    public static final String TOURNAMENT_JOIN_LABEL = "tournament_join";
    public static final String PLAYER_JOIN_LABEL = "player_tournament_join";

    //public long systemId;
    //public long stub;
    public long scheduleId;
    public long tournamentId;
    public int slot;
    public long instanceId;
    public long entryId;
    public boolean closed;
    public boolean finished;

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
        buffer.writeLong(tournamentId);
        buffer.writeInt(slot);
        buffer.writeLong(instanceId);
        buffer.writeLong(entryId);
        buffer.writeLong(stub);
        buffer.writeBoolean(closed);
        buffer.writeBoolean(finished);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        scheduleId = buffer.readLong();
        tournamentId = buffer.readLong();
        slot = buffer.readInt();
        instanceId = buffer.readLong();
        entryId = buffer.readLong();
        stub = buffer.readLong();
        closed = buffer.readBoolean();
        finished = buffer.readBoolean();
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
        jsonObject.addProperty("tournamentId",Long.toString(tournamentId));
        jsonObject.addProperty("slot",slot);
        jsonObject.addProperty("instanceId",Long.toString(instanceId));
        jsonObject.addProperty("entryId",Long.toString(entryId));
        jsonObject.addProperty("stub",Long.toString(stub));
        jsonObject.addProperty("closed",closed);
        jsonObject.addProperty("finished",finished);
        return jsonObject;
    }

    public void onTournament(long tournamentId,long segmentInstanceId,long entryId){
        ownerKey = SnowflakeKey.from(tournamentId);
        this.dataStore.createEdge(this,TOURNAMENT_JOIN_LABEL);
        this.tournamentId = tournamentId;
        this.closed = false;
        this.finished = false;
        this.instanceId = segmentInstanceId;
        this.entryId = entryId;
        this.dataStore.update(this);
    }
    public void onTournament(long tournamentId,int slot,long instanceId){
        ownerKey = SnowflakeKey.from(tournamentId);
        this.dataStore.createEdge(this,TOURNAMENT_JOIN_LABEL);
        this.tournamentId = tournamentId;
        this.closed = false;
        this.finished = false;
        this.slot = slot;
        this.instanceId = instanceId;
        this.dataStore.update(this);
    }
    public void finished(){
        finished = true;
        this.dataStore.update(this);
    }

    public static TournamentJoin lookup(DataStore dataStore,Session session,long tournamentId){
        TournamentJoin[] joined = new TournamentJoin[]{null};
        dataStore.list(new TournamentJoinQuery(SnowflakeKey.from(session.distributionId()),TournamentJoin.PLAYER_JOIN_LABEL),join->{
            if(join.tournamentId == tournamentId){
                join.dataStore(dataStore);
                joined[0]=join;
                return false;
            }
            return true;
        });
        if(joined[0]!=null) return joined[0];
        TournamentJoin tournamentJoin = new TournamentJoin(session.distributionId(),0);
        tournamentJoin.tournamentId = tournamentId;
        tournamentJoin.closed = true;
        tournamentJoin.ownerKey(SnowflakeKey.from(session.distributionId()));
        tournamentJoin.dataStore(dataStore);
        dataStore.create(tournamentJoin);
        return tournamentJoin;
    }
}
