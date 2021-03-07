package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentInstance extends RecoverableObject implements Tournament.Instance {

    private ConcurrentHashMap<String, Tournament.Entry> entryIndex = new ConcurrentHashMap<>();
    private Tournament.Status status = Tournament.Status.STARTED;
    private int maxEntries;
    private LocalDateTime start;
    private LocalDateTime close;
    private LocalDateTime end;


    public TournamentInstance(int maxEntries,LocalDateTime start,LocalDateTime close,LocalDateTime end){
        this();
        this.maxEntries = maxEntries;
        this.start =start;
        this.close = close;
        this.end = end;
    }
    public TournamentInstance(){
        this.onEdge = true;
        this.label = Tournament.INSTANCE_LABEL;
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
    public void enter(Tournament.Entry entry) {
        this.entryIndex.putIfAbsent(entry.systemId(),entry);
    }

    @Override
    public void update(String systemId, Tournament.OnEntry updater) {
        entryIndex.computeIfPresent(systemId,(k,v)->{
            updater.on(v);
            return v;
        });
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
        ArrayList<Tournament.Entry> entries = new ArrayList<>();
        entryIndex.forEach((k,v)->{
            entries.add(v);
        });
        return entries;
    }
    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_INSTANCE_CID;
    }
}
