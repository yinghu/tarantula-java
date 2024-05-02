package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.DataStore;
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

import java.util.concurrent.atomic.AtomicInteger;


public class TournamentInstance extends RecoverableObject implements Tournament.Instance, Portable {

    private Tournament.Status status = Tournament.Status.PENDING;
    private int maxEntries;
    private double scoreCredits;
    private LocalDateTime start;
    private LocalDateTime close;
    private LocalDateTime end;

    private AtomicInteger totalJoined;
    private AtomicInteger totalFinished;

    private boolean global;
    private double targetScore;

    private RaceBoardSync tournamentRaceBoard;

    public DataStore entryDataStore;
    public DataStore raceBoardDataStore;

    private TournamentEntryComparator entryComparator;

    private TournamentInstance(int maxEntries,double scoreCredits){
        this();
        this.maxEntries = maxEntries;
        this.scoreCredits = scoreCredits;
    }

    private TournamentInstance(double targetScore,int maxEntries){
        this();
        this.global = true;
        this.maxEntries = maxEntries;
        this.targetScore = targetScore;
    }

    public TournamentInstance(){
        totalFinished = new AtomicInteger(0);
        totalJoined = new AtomicInteger(0);
        this.onEdge = true;
    }

    @Override
    public Tournament.Status status(){
        return status;
    }


    public boolean scoreSegment(long entryId,long systemId,double credits,double score){
        if(!global) return false;
        TournamentEntry entry = new TournamentEntry();
        entry.distributionId(entryId);
        entry.dataStore(entryDataStore);
        if(!entryDataStore.load(entry) || entry.systemId()!= systemId) return false;
        entry.score(credits,score);
        this.tournamentRaceBoard.onBoard(entry);
        return true;
    }

    public long enterSegment(long systemId,double score){
        if(!global) return 0;
        if(targetScore>0){//limited winning players by first-in
            if(totalJoined.incrementAndGet()<maxEntries){
                TournamentEntry entry = new TournamentEntry(systemId,scoreCredits,score);
                entry.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                entry.ownerKey(this.key());
                this.entryDataStore.create(entry);
                entry.dataStore(entryDataStore);
                this.dataStore.update(this);
                tournamentRaceBoard.onBoard(entry);
                return entry.distributionId();
            }
            return 0;
        }
        //no limited competing players by higher score finally
        totalJoined.incrementAndGet();
        TournamentEntry entry = new TournamentEntry(systemId,scoreCredits,score);
        entry.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        entry.ownerKey(this.key());
        entry.dataStore(entryDataStore);
        entryDataStore.create(entry);
        this.dataStore.update(this);
        this.tournamentRaceBoard.onBoard(entry);
        return entry.distributionId();
    }


    public boolean enter(long systemId,double score) {
        if(global) return false;
        if(totalJoined.get()==maxEntries) return false;
        TournamentEntry entry = new TournamentEntry(systemId,scoreCredits,score);
        entry.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        entry.ownerKey(this.key());
        this.entryDataStore.create(entry);
        entry.dataStore(entryDataStore);
        totalJoined.incrementAndGet();
        this.dataStore.update(this);
        tournamentRaceBoard.onBoard(entry);
        return true;

    }

    @Override
    public boolean update(Session session, Tournament.OnEntry onEntry) {
        if(global) return false;
        Tournament.Entry entry = tournamentRaceBoard.onBoard(session.distributionId());
        if(onEntry.on(entry)) totalFinished.incrementAndGet();
        int finished = totalFinished.get();
        int joined = totalJoined.get();
        return status == (Tournament.Status.CLOSED)? (finished == joined):(finished == joined && joined == maxEntries);
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
        buffer.writeBoolean(global);
        buffer.writeDouble(targetScore);
        buffer.writeInt(totalJoined.get());
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
        global = buffer.readBoolean();
        targetScore = buffer.readDouble();
        totalJoined.set(buffer.readInt());
        return true;
    }

    public Tournament.RaceBoard raceBoard(){
        return new TournamentRaceBoard(tournamentRaceBoard.snapshot());
    }
    public Tournament.RaceBoard myRaceBoard(){
        return null;
    }
    public Tournament.RaceBoard myRaceBoard(long entryId,long systemId,int size){
        ArrayList<Tournament.Entry> sorted = new ArrayList<>();
        TournamentEntry me = new TournamentEntry();
        me.distributionId(entryId);
        if(!entryDataStore.load(me) || me.systemId() != systemId) return new TournamentRaceBoard(sorted);

        sorted.add(me.duplicate(0));
        int[] ahead = {size-2};
        int[] after = {1};
        entryDataStore.list(new TournamentEntryQuery(this.distributionId),(e)->{
            if(e.systemId()==systemId) return true;
            if(e.score() > me.score() && ahead[0]>0){
                sorted.add(e.duplicate(0));
                ahead[0]--;
            }
            else if(e.score()<me.score() && after[0]>0){
                sorted.add(e.duplicate(0));
                after[0]--;
            }
            return !(ahead[0]==0 && after[0]==0);
        });
        Collections.sort(sorted,entryComparator);
        for(int i=0; i<sorted.size();i++){
            sorted.get(i).rank(i+1);
        }
        return new TournamentRaceBoard(sorted);
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
        entryComparator = new TournamentEntryComparator();
        tournamentRaceBoard = new RaceBoardSync(maxEntries,entryComparator);
        tournamentRaceBoard.dataStore(raceBoardDataStore);
        tournamentRaceBoard.distributionId(this.distributionId);
        tournamentRaceBoard.load();
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("MaxEntries",maxEntries);
        jsonObject.addProperty("Start",start!=null?start.format(DateTimeFormatter.ISO_DATE_TIME):"N/A");
        jsonObject.addProperty("Close",close!=null?close.format(DateTimeFormatter.ISO_DATE_TIME):"N/A");
        jsonObject.addProperty("End",end!=null?end.format(DateTimeFormatter.ISO_DATE_TIME):"N/A");
        jsonObject.addProperty("Status",status.name());
        jsonObject.addProperty("TournamentId",this.distributionKey());
        jsonObject.addProperty("Global",global);
        jsonObject.addProperty("TotalJoined",totalJoined.get());
        jsonObject.addProperty("TargetScore",targetScore);
        return jsonObject;
    }
    List<TournamentEntry> sorted(){
        List<TournamentEntry> rankedList = entryDataStore.list(new TournamentEntryQuery(this.distributionId));
        Collections.sort(rankedList,entryComparator);
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

    public static TournamentInstance global(double targetScore,int maxEntries){
        return new TournamentInstance(targetScore,maxEntries);
    }
    public static TournamentInstance limit(int maxEntries,double credits){
        return new TournamentInstance(maxEntries,credits);
    }

}
