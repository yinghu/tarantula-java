package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.Map;

public class DefaultTournament extends RecoverableObject implements Tournament {

    private String type;
    private LocalDateTime startTime;
    private LocalDateTime closeTime;
    private LocalDateTime endTime;
    private int maxEntriesPerInstance;
    private int durationMinutes;
    private Listener listener;
    private Creator creator;

    public DefaultTournament(String type,Schedule schedule,Creator creator,Listener listener){
        this.type = type;
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
    public LocalDateTime startTime() {
        return startTime;
    }
    public Map<String,Object> toMap(){
        properties.put("1",type);
        properties.put("2", SystemUtil.toUTCMilliseconds(startTime));
        properties.put("3", SystemUtil.toUTCMilliseconds(closeTime));
        properties.put("4", SystemUtil.toUTCMilliseconds(endTime));
        properties.put("5",maxEntriesPerInstance);
        properties.put("6",durationMinutes);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("1");
        this.startTime = SystemUtil.fromUTCMilliseconds(((Number)properties.get("2")).longValue());
        this.closeTime = SystemUtil.fromUTCMilliseconds(((Number)properties.get("3")).longValue());
        this.endTime = SystemUtil.fromUTCMilliseconds(((Number)properties.get("4")).longValue());
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
    public Instance join(String systemId) {
        Instance instance = creator.create(this);
        instance.enter(creator.create(systemId,instance));
        listener.onCreated(instance);
        return instance;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.TOURNAMENT_CID;
    }

}
