package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.event.PortableEventRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TournamentHistoryRecord extends RecoverableObject implements Tournament.Instance{

    protected Tournament.Status status = Tournament.Status.STARTED;
    protected int maxEntries;
    protected LocalDateTime start;
    protected LocalDateTime close;
    protected LocalDateTime end;

    private ConcurrentHashMap<String, TournamentEntry> entryIndex = new ConcurrentHashMap<>();
    private TournamentRaceBoard tournamentRaceBoard = new TournamentRaceBoard();

    public TournamentHistoryRecord(){

    }
    @Override
    public Tournament.Status status(){
        return status;
    }
    @Override
    public int enter(String systemId) {
        entryIndex.computeIfAbsent(systemId,(k)->{
            TournamentEntry entry = new TournamentEntry(systemId,this.distributionKey(),0);
            this.dataStore.create(entry);
            entry.dataStore(dataStore);
            tournamentRaceBoard.addEntry(entry);
            return entry;
        });
        return entryIndex.size();
    }

    @Override
    public boolean update(String systemId, Tournament.OnEntry updater) {
        TournamentEntry entry = entryIndex.get(systemId);
        updater.on(entry);
        return false;
    }
    public int finish(String systemId){
        return 1;
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


    public boolean load(){
        dataStore.list(new TournamentEntryQuery(this.distributionId()),(e)->{
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
        jsonObject.add("board",tournamentRaceBoard.toJson());
        return jsonObject;
    }

}
