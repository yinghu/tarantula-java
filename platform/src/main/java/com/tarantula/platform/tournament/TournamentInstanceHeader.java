package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TournamentInstanceHeader extends RecoverableObject implements Tournament.Instance, Portable {

    protected Tournament.Status status = Tournament.Status.STARTED;
    protected int maxEntries;
    protected LocalDateTime start;
    protected LocalDateTime close;
    protected LocalDateTime end;

    private ConcurrentHashMap<String, TournamentEntry> entryIndex = new ConcurrentHashMap<>();
    private TournamentRaceBoard tournamentRaceBoard = new TournamentRaceBoard();

    public TournamentInstanceHeader(int maxEntries, LocalDateTime start, LocalDateTime close, LocalDateTime end){
        this.maxEntries = maxEntries;
        this.start = start;
        this.close = close;
        this.end = end;
    }
    public TournamentInstanceHeader(){

    }
    @Override
    public Tournament.Status status(){
        return status;
    }
    @Override
    public Tournament.Entry join(String systemId) {
        return entryIndex.computeIfAbsent(systemId,(k)->{
            TournamentEntry entry = new TournamentEntry(systemId,this.distributionKey());
            this.dataStore.create(entry);
            entry.dataStore(dataStore);
            tournamentRaceBoard.addEntry(entry);
            return entry;
        });
    }

    @Override
    public void update(String systemId, Tournament.OnEntry updater) {
        TournamentEntry entry = entryIndex.get(systemId);
        updater.on(entry);
    }
    public int maxEntries(){
        return maxEntries;
    }
    public LocalDateTime startTime(){
        return start;
    }
    public LocalDateTime closeTime(){
        return close;
    }
    public LocalDateTime endTime(){
        return end;
    }
    public Map<String,Object> toMap(){
        properties.put("1",maxEntries);
        properties.put("2", TimeUtil.toUTCMilliseconds(start));
        properties.put("3", TimeUtil.toUTCMilliseconds(close));
        properties.put("4", TimeUtil.toUTCMilliseconds(end));
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.maxEntries = ((Number)properties.get("1")).intValue();
        this.start = TimeUtil.fromUTCMilliseconds(((Number)properties.get("2")).longValue());
        this.close = TimeUtil.fromUTCMilliseconds(((Number)properties.get("3")).longValue());
        this.end = TimeUtil.fromUTCMilliseconds(((Number)properties.get("4")).longValue());
    }
    public Tournament.RaceBoard raceBoard(){
        tournamentRaceBoard.reset();
        return tournamentRaceBoard;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TOURNAMENT_INSTANCE_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeLong("1",TimeUtil.toUTCMilliseconds(start));
        portableWriter.writeLong("2",TimeUtil.toUTCMilliseconds(close));
        portableWriter.writeLong("3",TimeUtil.toUTCMilliseconds(end));
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.start = TimeUtil.fromUTCMilliseconds(portableReader.readLong("1"));
        this.close = TimeUtil.fromUTCMilliseconds(portableReader.readLong("2"));
        this.end = TimeUtil.fromUTCMilliseconds(portableReader.readLong("3"));
    }

    public boolean load(){
        dataStore.list(new TournamentEntryQuery(this.distributionKey()),(e)->{
            e.dataStore(dataStore);
            entryIndex.put(e.systemId(),e);
            tournamentRaceBoard.addEntry(e);
            return true;
        });
        return true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("start",start.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("close",close.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("end",end.format(DateTimeFormatter.ISO_DATE_TIME));
        return jsonObject;
    }
    List<TournamentEntry> end(){
        ArrayList<TournamentEntry> rankedList = new ArrayList<>();
        entryIndex.forEach((k,e)->rankedList.add(e));
        Collections.sort(rankedList,new TournamentEntryComparator());
        return rankedList;
    }

}
