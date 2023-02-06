package com.tarantula.platform.tournament;

import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TournamentSchedule extends Application {


    public TournamentSchedule(){

    }

    public TournamentSchedule(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public String name(){
        return header.get("Name").getAsString();
    }

    public String type() {
        return header.get("Type").getAsString();
    }

    public String description() {
        return header.get("Description").getAsString();
    }

    public int schedule(){ return header.get("Schedule").getAsInt();}

    public double enterCost(){ return header.get("EnterCost").getAsDouble();}
    public LocalDateTime startTime() {
        return TimeUtil.fromString("yyyy-MM-dd'T'HH:mm",header.get("StartTime").getAsString());
    }

    public int durationHoursPerSchedule() {
        return header.get("DurationHoursPerSchedule").getAsInt();
    }


    public int maxEntriesPerInstance() {
        return header.get("MaxEntriesPerInstance").getAsInt();
    }

    public int durationMinutesPerInstance() {
        return header.get("DurationMinutesPerInstance").getAsInt();
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

    public List<TournamentPrize> list(){
        ArrayList<TournamentPrize> prizes = new ArrayList<>();
        _reference.forEach(c->{
            TournamentPrize prize = new TournamentPrize(c);
            prize.configureAndValidate();
            prizes.add(prize);
        });
        return prizes;
    }

    public TournamentScheduleStatus status(){
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionKey(this.distributionKey());
        return status;
    }

}
