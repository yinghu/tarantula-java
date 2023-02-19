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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class TournamentInstance extends RecoverableObject implements Tournament.Instance, Portable {

    private Tournament.Status status = Tournament.Status.PENDING;
    private int maxEntries;
    private double scoreCredits;
    private LocalDateTime start;
    private LocalDateTime close;
    private LocalDateTime end;

    private int totalJoined;
    private AtomicInteger totalFinished;

    private ConcurrentHashMap<String, TournamentEntry> entryIndex = new ConcurrentHashMap<>();
    private TournamentRaceBoard tournamentRaceBoard = new TournamentRaceBoard();

    ScheduledFuture<?> pendingSchedule;


    public TournamentInstance(int maxEntries){
        this();
        this.maxEntries = maxEntries;
    }
    public TournamentInstance(){
        totalFinished = new AtomicInteger();
    }

    @Override
    public Tournament.Status status(){
        return status;
    }

    @Override
    public int enter(String systemId) {
        entryIndex.computeIfAbsent(systemId,(k)->{
            TournamentEntry entry = new TournamentEntry(systemId,this.distributionKey(),scoreCredits);
            this.dataStore.create(entry);
            entry.dataStore(dataStore);
            tournamentRaceBoard.addEntry(entry);
            totalJoined++;
            return entry;
        });
        return totalJoined;
    }

    @Override
    public boolean update(String systemId, Tournament.OnEntry updater) {
        TournamentEntry entry = entryIndex.get(systemId);
        if(updater.on(entry)) totalFinished.incrementAndGet();
        int finished = totalFinished.get();
        return status == (Tournament.Status.CLOSED)? (finished == totalJoined):(finished == totalJoined && totalJoined == maxEntries);
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
        properties.put("2", start!=null?TimeUtil.toUTCMilliseconds(start):0);
        properties.put("3", close!=null?TimeUtil.toUTCMilliseconds(close):0);
        properties.put("4", end!=null?TimeUtil.toUTCMilliseconds(end):0);
        properties.put("5",status.name());
        properties.put("6",routingNumber);
        return properties;
    }

    public void fromMap(Map<String,Object> properties){
        this.maxEntries = ((Number)properties.get("1")).intValue();
        this.start = TimeUtil.fromUTCMilliseconds(((Number)properties.getOrDefault("2",0)).longValue());
        this.close = TimeUtil.fromUTCMilliseconds(((Number)properties.getOrDefault("3",0)).longValue());
        this.end = TimeUtil.fromUTCMilliseconds(((Number)properties.getOrDefault("4",0)).longValue());
        this.status = Tournament.Status.valueOf((String) properties.get("5"));
        this.routingNumber = ((Number)properties.getOrDefault("6",0)).intValue();
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

    public void load(){
        dataStore.list(new TournamentEntryQuery(this.distributionKey()),(e)->{
            e.dataStore(dataStore);
            entryIndex.put(e.systemId(),e);
            tournamentRaceBoard.addEntry(e);
            totalJoined++;
            return true;
        });
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("MaxEntries",maxEntries);
        jsonObject.addProperty("Start",start.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("Close",close.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("End",end.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("Status",status.name());
        jsonObject.addProperty("TournamentId",this.distributionKey());
        return jsonObject;
    }
    List<TournamentEntry> end(){
        ArrayList<TournamentEntry> rankedList = new ArrayList<>();
        entryIndex.forEach((k,e)->rankedList.add(e));
        Collections.sort(rankedList,new TournamentEntryComparator());
        return rankedList;
    }

    void started(LocalDateTime start,LocalDateTime close,LocalDateTime end,double scoreCredits){
        this.scoreCredits = scoreCredits;
        this.start = start;
        this.close = close;
        this.end = end;
        this.status = Tournament.Status.STARTED;
    }
    void starting(int queueNumber){
        this.routingNumber = queueNumber;
        this.status = Tournament.Status.STARTING;
    }
    void closed(){
        this.status = Tournament.Status.CLOSED;
    }
    void ended(){
        this.status = Tournament.Status.ENDED;
    }

    public String toString(){
        return "Tournament ["+distributionKey()+"]["+status+"]["+maxEntries+"]["+routingNumber+"]";
    }

    long toClosingTime(){
        if(TimeUtil.expired(close)) return 10;
        return TimeUtil.durationUTCMilliseconds(start,close);
    }
    long toEndingTime(){
        if(TimeUtil.expired(end)) return 10;
        return TimeUtil.durationUTCMilliseconds(close,end);
    }

}
