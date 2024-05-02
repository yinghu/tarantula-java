package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TournamentHistory extends RecoverableObject implements Tournament.History {

    public long tournamentId;
    public long instanceId;
    public long entryId;
    public long prizeId;
    public LocalDateTime dateTime;

    public TournamentHistory(){
        this.onEdge = true;
        this.label = Tournament.HISTORY_LABEL;
    }
    public TournamentHistory(long tournamentId,long instanceId,long entryId,long prizeId,LocalDateTime dateTime){
        this();
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
        this.entryId = entryId;
        this.prizeId = prizeId;
        this.dateTime = dateTime;
    }


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(tournamentId);
        buffer.writeLong(instanceId);
        buffer.writeLong(entryId);
        buffer.writeLong(prizeId);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(dateTime));
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        tournamentId = buffer.readLong();
        instanceId = buffer.readLong();
        entryId = buffer.readLong();
        prizeId = buffer.readLong();
        dateTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        return true;
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_HISTORY_CID;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("TournamentId",Long.toString(tournamentId));
        jsonObject.addProperty("InstanceId",Long.toString(instanceId));
        jsonObject.addProperty("EntryId",Long.toString(entryId));
        jsonObject.addProperty("DataTime",dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        return jsonObject;
    }

    @Override
    public long tournamentId() {
        return tournamentId;
    }

    @Override
    public long instanceId() {
        return instanceId;
    }

    @Override
    public long entryId() {
        return entryId;
    }

    public long prizeId(){
        return prizeId;
    }
    @Override
    public LocalDateTime dateTime() {
        return dateTime;
    }

    public static void list(long systemId, DataStore.Stream<TournamentHistory> stream,DataStore dataStore){
        dataStore.list(new TournamentHistoryQuery(systemId),(tournamentHistory -> stream.on(tournamentHistory)));
    }
}
