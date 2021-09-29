package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.ConfigurableObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TournamentScheduleParser extends ConfigurableObject {

    private static final int ON_DEMAND_START_DELAY_MINUTES = 5;
    private static final int ENDING_BUFFER_MINUTES = 10;
    private static final int DAILY_MINUTES = 1440;
    private static final int WEEKLY_MINUTES = 10080;
    private static final int MONTHLY_MINUTES = 43200;

    public TournamentScheduleParser(){}


    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_PARSER_CID;
    }

    public Tournament.Schedule parse(){
        String type = header.get("type").getAsString();
        String name = header.get("name").getAsString();
        String _schedule = header.get("schedule").getAsString();
        DefaultTournamentSchedule schedule;
        if(_schedule.equals(Tournament.ON_DEMAND_SCHEDULE)){
            LocalDateTime _start = LocalDateTime.now().plusMinutes(ON_DEMAND_START_DELAY_MINUTES);//delay 5 minutes to start
            long durationMinutes = application.get("durationHours").getAsLong()*60;
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(durationMinutes-minutes);
            LocalDateTime _end  = _start.plusMinutes(durationMinutes+ENDING_BUFFER_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.ON_DEMAND_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.DAILY_SCHEDULE)){//RUN ONE DAY FROM MIDNIGHT
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(DAILY_MINUTES-minutes);
            LocalDateTime _end  = _start.plusMinutes(DAILY_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.DAILY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.WEEKLY_SCHEDULE)){//RUN 7 DAYS FROM START DAY
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(WEEKLY_MINUTES-minutes);
            LocalDateTime _end  = _start.plusMinutes(WEEKLY_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.WEEKLY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.MONTHLY_SCHEDULE)){//RUN 30 DAYS FROM START DAY
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(MONTHLY_MINUTES-minutes);
            LocalDateTime _end  = _start.plusHours(MONTHLY_MINUTES);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.MONTHLY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else{
            throw new UnsupportedOperationException(_schedule+" not supported");
        }
        schedule.distributionKey(this.distributionKey());
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
