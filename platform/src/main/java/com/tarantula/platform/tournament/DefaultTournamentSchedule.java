package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.Map;

public class DefaultTournamentSchedule extends RecoverableObject implements Tournament.Schedule {

    private String type;

    private LocalDateTime start;
    private LocalDateTime close;
    private LocalDateTime end;
    private int duration;
    private int maxEntries;
    private String schedule;


    public DefaultTournamentSchedule(){

    }

    public DefaultTournamentSchedule(String type, String name, String schedule,LocalDateTime start, LocalDateTime close, LocalDateTime end, int duration, int maxEntries){
        this.type = type;
        this.name = name;
        this.schedule = schedule;
        this.start = start;
        this.close = close;
        this.end = end;
        this.duration = duration;
        this.maxEntries = maxEntries;
    }
    @Override
    public String type() {
        return type;
    }

    @Override
    public String schedule(){ return schedule;}
    @Override
    public LocalDateTime startTime() {
        return start;
    }

    @Override
    public LocalDateTime closeTime() {
        return close;
    }

    @Override
    public LocalDateTime endTime() {
        return end;
    }

    @Override
    public int maxEntriesPerInstance() {
        return maxEntries;
    }

    @Override
    public int instanceDurationInMinutes() {
        return duration;
    }
    public Map<String,Object> toMap(){
        properties.put("type",type);
        properties.put("name",name);
        properties.put("schedule",schedule);
        properties.put("start", TimeUtil.toUTCMilliseconds(start));
        properties.put("close",TimeUtil.toUTCMilliseconds(close));
        properties.put("end",TimeUtil.toUTCMilliseconds(end));
        properties.put("maxEntries",maxEntries);
        properties.put("duration",duration);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.type = (String) properties.get("type");
        this.name = (String) properties.get("name");
        this.schedule = (String) properties.get("schedule");
        this.start = TimeUtil.fromUTCMilliseconds(((Number)properties.get("start")).longValue());
        this.close = TimeUtil.fromUTCMilliseconds(((Number)properties.get("close")).longValue());
        this.end = TimeUtil.fromUTCMilliseconds(((Number)properties.get("end")).longValue());
        this.maxEntries = ((Number)properties.get("maxEntries")).intValue();
        this.duration = ((Number)properties.get("duration")).intValue();
    }
    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_CID;
    }

}
