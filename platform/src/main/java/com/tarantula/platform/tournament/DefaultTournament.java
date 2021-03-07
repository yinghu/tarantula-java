package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DefaultTournament extends RecoverableObject implements Tournament {

    private String type;
    private String description;
    private String icon;
    private Status status = Status.SCHEDULED;
    private LocalDateTime startTime;
    private LocalDateTime closeTime;
    private LocalDateTime endTime;
    private int maxEntriesPerInstance;
    private int durationMinutes;
    private Listener listener;
    private Creator creator;

    private ConcurrentLinkedDeque<Instance> pendingQueue = new ConcurrentLinkedDeque();
    private ConcurrentHashMap<String,Entry> entryIndex = new ConcurrentHashMap<>();

    public DefaultTournament(Schedule schedule,Creator creator,Listener listener){
        this.type = schedule.type();
        this.description = schedule.description();
        this.icon = schedule.icon();
        this.startTime = schedule.startTime();
        this.closeTime = schedule.closeTime();
        this.endTime = schedule.endTime();
        this.maxEntriesPerInstance = schedule.maxEntriesPerInstance();
        this.durationMinutes = schedule.instanceDurationInMinutes();
        this.creator = creator;
        this.listener = listener;
    }
    public DefaultTournament(Creator creator,Listener listener){
        this.creator = creator;
        this.listener = listener;
    }
    public DefaultTournament(){
    }
    @Override
    public String type() {
        return type;
    }
    @Override
    public String description() {
        return description;
    }
    @Override
    public String icon() {
        return icon;
    }

    @Override
    public Status status(){
        return status;
    }
    @Override
    public LocalDateTime startTime() {
        return startTime;
    }
    public Map<String,Object> toMap(){
        properties.put("1",type);
        properties.put("2", TimeUtil.toUTCMilliseconds(startTime));
        properties.put("3", TimeUtil.toUTCMilliseconds(closeTime));
        properties.put("4", TimeUtil.toUTCMilliseconds(endTime));
        properties.put("5",maxEntriesPerInstance);
        properties.put("6",durationMinutes);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("1");
        this.startTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("2")).longValue());
        this.closeTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("3")).longValue());
        this.endTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("4")).longValue());
        this.maxEntriesPerInstance = ((Number)properties.get("5")).intValue();
        this.durationMinutes = ((Number)properties.get("6")).intValue();
    }
    @Override
    public LocalDateTime closeTime() {
        return closeTime;
    }

    @Override
    public LocalDateTime endTime() {
        return endTime;
    }
    public int maxEntriesPerInstance(){
        return maxEntriesPerInstance;
    }
    public int durationMinutesPerInstance(){
        return durationMinutes;
    }


    @Override
    public Entry join(String systemId) {
        Entry _entry = entryIndex.get(systemId);
        if(_entry!=null){//rejoin
            return _entry;
        }
        Instance instance = pendingQueue.poll();
        if(instance==null){
            instance = creator.create(this);
            listener.onStarted(instance);
        }
        Entry entry = creator.create(systemId,instance);
        instance.enter(creator.create(systemId,instance));
        pendingQueue.offer(instance);
        return entry;
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_CID;
    }
    //local methods
    public void addTournamentInstance(Instance instance){
        pendingQueue.add(instance);
    }
    public void addTournamentEntry(Entry entry){
        entryIndex.put(entry.systemId(),entry);
    }
}
