package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.ConfigurableObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TournamentScheduleParser extends ConfigurableObject {

    private static final int ON_DEMAND_START_DELAY_MINUTES = 5;
    private static final int MIN_ON_DEMAND_DURATION_MINUTES = 60;
    private static final int MIN_DURATION_MINUTES_PER_INSTANCE = 3;
    private static final int ENDING_BUFFER_MINUTES = 10;
    private static final int DAILY_MINUTES = 1440;
    private static final int WEEKLY_MINUTES = 10080;
    private static final int MONTHLY_MINUTES = 43200;

    public TournamentScheduleParser(){}

    public void schedule(JsonObject application){
        this.application = application;
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_PARSER_CID;
    }

    public DefaultTournamentSchedule schedule(){
        String type = header.get("type").getAsString();
        String name = header.get("name").getAsString();
        String _schedule = header.get("schedule").getAsString();
        int entries = header.get("maxEntriesPerInstance").getAsInt();
        DefaultTournamentSchedule schedule;
        if(_schedule.equals(Tournament.ON_DEMAND_SCHEDULE)){
            LocalDateTime _start = LocalDateTime.now().plusMinutes(ON_DEMAND_START_DELAY_MINUTES);//delay 5 minutes to start
            long durationMinutes = application.get("durationHours").getAsLong()*60;
            if(durationMinutes<MIN_ON_DEMAND_DURATION_MINUTES) throw new UnsupportedOperationException("on demand duration cannot be less < 1 hour");
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            if(minutes<MIN_DURATION_MINUTES_PER_INSTANCE) throw new UnsupportedOperationException("duration per instance cannot be less < 3 minutes");
            LocalDateTime _close = _start.plusMinutes(durationMinutes-minutes);
            LocalDateTime _end  = _start.plusMinutes(durationMinutes+ENDING_BUFFER_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.ON_DEMAND_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.DAILY_SCHEDULE)){//RUN ONE DAY FROM MIDNIGHT
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            if(TimeUtil.expired(_start)) throw new RuntimeException(_start.format(DateTimeFormatter.ISO_DATE_TIME)+" is expired");
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            if(minutes<MIN_DURATION_MINUTES_PER_INSTANCE) throw new UnsupportedOperationException("duration per instance cannot be less < 3 minutes");
            LocalDateTime _close = _start.plusMinutes(DAILY_MINUTES-minutes);
            LocalDateTime _end  = _start.plusMinutes(DAILY_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.DAILY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.WEEKLY_SCHEDULE)){//RUN 7 DAYS FROM START DAY
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            if(TimeUtil.expired(_start)) throw new RuntimeException(_start.format(DateTimeFormatter.ISO_DATE_TIME)+" is expired");
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            if(minutes<MIN_DURATION_MINUTES_PER_INSTANCE) throw new UnsupportedOperationException("duration per instance cannot be less < 3 minutes");
            LocalDateTime _close = _start.plusMinutes(WEEKLY_MINUTES-minutes);
            LocalDateTime _end  = _start.plusMinutes(WEEKLY_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.WEEKLY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.MONTHLY_SCHEDULE)){//RUN 30 DAYS FROM START DAY
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            if(TimeUtil.expired(_start)) throw new RuntimeException(_start.format(DateTimeFormatter.ISO_DATE_TIME)+" is expired");
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            if(minutes<MIN_DURATION_MINUTES_PER_INSTANCE) throw new UnsupportedOperationException("duration per instance cannot be less < 3 minutes");
            LocalDateTime _close = _start.plusMinutes(MONTHLY_MINUTES-minutes);
            LocalDateTime _end  = _start.plusHours(MONTHLY_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.MONTHLY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else{
            throw new UnsupportedOperationException(_schedule+" not supported");
        }
        schedule.index(this.distributionKey());
        validatePrize();
        return schedule;
    }
    private void validatePrize(){
        reference.forEach((refId)->{
            TournamentPrize configurableObject = new TournamentPrize();
            configurableObject.distributionKey(refId.getAsString());
            if(dataStore.load(configurableObject)){
                configurableObject.dataStore(dataStore);
                if(!configurableObject.configureAndValidate()){
                    throw new RuntimeException("No tournament prize");
                }
            }
        });
    }
    public Map<Integer,TournamentPrize> prize(){
        Map<Integer,TournamentPrize> _prizeMap = new HashMap<>();
        reference.forEach((refId)->{
            TournamentPrize configurableObject = new TournamentPrize();
            configurableObject.distributionKey(refId.getAsString());
            if(dataStore.load(configurableObject)){
                configurableObject.dataStore(dataStore);
                if(configurableObject.configureAndValidate()){
                    configurableObject.configurationCategory(this.configurationCategory);
                    _prizeMap.put(configurableObject.rank(),configurableObject);
                }
            }
        });
        return _prizeMap;
    }
}
