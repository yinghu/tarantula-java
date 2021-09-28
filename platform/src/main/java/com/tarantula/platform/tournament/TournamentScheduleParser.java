package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.ConfigurableObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TournamentScheduleParser extends ConfigurableObject {


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
        String _schedule = Tournament.DAILY_SCHEDULE;
        if(application.has("schedule")){
            _schedule = application.get("schedule").getAsString();
        }
        DefaultTournamentSchedule schedule;
        if(_schedule.equals(Tournament.ON_DEMAND_SCHEDULE)){
            LocalDateTime _start = LocalDateTime.now().plusMinutes(5);//delay 5 minutes to start
            long hours = application.get("durationHours").getAsLong();
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(hours*60-minutes);
            LocalDateTime _end  = _start.plusHours(hours);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.ON_DEMAND_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.DAILY_SCHEDULE)){//RUN ONE DAY FROM MIDNIGHT
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(24*60-minutes);
            LocalDateTime _end  = _start.plusHours(24);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.DAILY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.WEEKLY_SCHEDULE)){//RUN 7 DAYS FROM START DAY
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(24*60-minutes);
            LocalDateTime _end  = _start.plusHours(24);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.DAILY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else if(_schedule.equals(Tournament.MONTHLY_SCHEDULE)){//RUN 30 DAYS FROM START DAY
            LocalDateTime _start = TimeUtil.toMidnight(application.get("startDate").getAsString());
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(24*60-minutes);
            LocalDateTime _end  = _start.plusHours(24);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.DAILY_SCHEDULE,_start,_close,_end,minutes,entries);
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
