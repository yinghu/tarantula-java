package com.tarantula.platform.tournament;

import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

import java.time.LocalDateTime;

public class TournamentSchedule extends Application {

    private String type;

    private LocalDateTime start;
    private LocalDateTime close;
    private LocalDateTime end;
    private int duration;
    private int maxEntries;
    private int schedule;


    public TournamentSchedule(){

    }

    public TournamentSchedule(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public String name(){
        return header.get("Name").getAsString();
    }
    //@Override
    public String type() {
        return header.get("Type").getAsString();
    }

    //@Override
    public int schedule(){ return header.get("Schedule").getAsInt();}
    //@Override
    public LocalDateTime startTime() {
        return start;
    }

    //@Override
    public LocalDateTime closeTime() {
        return close;
    }

    ///@Override
    public LocalDateTime endTime() {
        return end;
    }

    //@Override
    public int maxEntriesPerInstance() {
        return header.get("MaxEntriesPerInstance").getAsInt();
    }

    //@Override
    public int instanceDurationInMinutes() {
        return header.get("DurationPerInstance").getAsInt();
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_CID;
    }

    @Override
    public boolean configureAndValidate() {
        setup();
        return validated;
    }

}
