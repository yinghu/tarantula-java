package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Session;
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
import java.util.concurrent.ConcurrentHashMap;
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

    private ConcurrentHashMap<Long, TournamentEntry> entryIndex = new ConcurrentHashMap<>();
    private TournamentRaceBoard tournamentRaceBoard;

    public TournamentInstance(int maxEntries,double scoreCredits){
        this();
        this.maxEntries = maxEntries;
        this.scoreCredits = scoreCredits;
        this.tournamentRaceBoard = new TournamentRaceBoard(maxEntries);
    }

    public TournamentInstance(){
        totalFinished = new AtomicInteger(0);
        this.onEdge = true;
    }

    @Override
    public Tournament.Status status(){
        return status;
    }

    public boolean enter(long systemId,double score) {
        return entryIndex.computeIfAbsent(systemId,(k)->{
            if(totalJoined==maxEntries) return null;
            TournamentEntry entry = new TournamentEntry(systemId,scoreCredits,score);
            entry.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            entry.ownerKey(this.key());
            this.dataStore.create(entry);
            entry.dataStore(dataStore);
            tournamentRaceBoard.addEntry(entry);
            totalJoined++;
            return entry;
        })!=null;
    }


    public int enter(long systemId){
        enter(systemId,0);
        return totalJoined;
    }

    @Override
    public boolean update(Session session, Tournament.OnEntry onEntry) {
        TournamentEntry entry = entryIndex.get(session.stub());
        if(onEntry.on(entry)) totalFinished.incrementAndGet();
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

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(maxEntries);
        buffer.writeDouble(scoreCredits);
        buffer.writeLong(start!=null?TimeUtil.toUTCMilliseconds(start):0);
        buffer.writeLong(close!=null?TimeUtil.toUTCMilliseconds(close):0);
        buffer.writeLong(end!=null?TimeUtil.toUTCMilliseconds(end):0);
        buffer.writeInt(status.ordinal());
        buffer.writeInt(routingNumber);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        maxEntries = buffer.readInt();
        scoreCredits = buffer.readDouble();
        long _start = buffer.readLong();
        if(_start>0) start = TimeUtil.fromUTCMilliseconds(_start);
        long _close = buffer.readLong();
        if(_close>0) close = TimeUtil.fromUTCMilliseconds(_close);
        long _end = buffer.readLong();
        if(_end>0) end = TimeUtil.fromUTCMilliseconds(_end);
        status = Tournament.Status.values()[buffer.readInt()];
        routingNumber = buffer.readInt();
        return true;
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
        dataStore.list(new TournamentEntryQuery(this.distributionId()),(e)->{
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
        jsonObject.addProperty("Start",start!=null?start.format(DateTimeFormatter.ISO_DATE_TIME):"N/A");
        jsonObject.addProperty("Close",close!=null?close.format(DateTimeFormatter.ISO_DATE_TIME):"N/A");
        jsonObject.addProperty("End",end!=null?end.format(DateTimeFormatter.ISO_DATE_TIME):"N/A");
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

    void started(LocalDateTime start,LocalDateTime close,LocalDateTime end){
        this.start = start;
        this.close = close;
        this.end = end;
        this.status = Tournament.Status.STARTED;
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
