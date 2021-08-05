package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TournamentInstanceHeader extends RecoverableObject implements Tournament.Instance {

    protected Tournament.Status status = Tournament.Status.STARTED;
    protected int maxEntries;
    protected LocalDateTime start;
    protected LocalDateTime close;
    protected LocalDateTime end;

    private ConcurrentHashMap<String, TournamentEntry> entryIndex = new ConcurrentHashMap<>();

    public TournamentInstanceHeader(int maxEntries, LocalDateTime start, LocalDateTime close, LocalDateTime end){
        this.maxEntries = maxEntries;
        this.start = start;
        this.close = close;
        this.end = end;
    }
    public TournamentInstanceHeader(){

    }

    @Override
    public String id() {
        return this.distributionKey();
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
    @Override
    public List<Tournament.Entry> list() {
        throw new UnsupportedOperationException();
    }
    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_INSTANCE_CID;
    }
    public void load(){
        dataStore.list(new TournamentEntryQuery(this.distributionKey()),(e)->{
            e.dataStore(dataStore);
            System.out.println("LOADING ENTRY->"+e.distributionKey()+">"+e.toJson());
            entryIndex.put(e.systemId(),e);
            return true;
        });
    }

}
