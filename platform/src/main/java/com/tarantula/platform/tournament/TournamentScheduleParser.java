package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.ConfigurableObject;


import java.time.LocalDateTime;

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
        else if(_schedule.equals(Tournament.DAILY_SCHEDULE)){
            LocalDateTime _start = TimeUtil.midnight();
            int minutes = application.get("durationMinutesPerInstance").getAsInt();
            int entries = application.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(24*60-minutes);
            LocalDateTime _end  = _start.plusHours(24);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.DAILY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else{
            throw new UnsupportedOperationException(_schedule+" not supported");
        }
        return schedule;
    }
    private void loadPrize(){
        reference.forEach((refId)->{
            ConfigurableObject item = new ConfigurableObject();
            item.distributionKey(refId.getAsString());
            this.dataStore.load(item);
            item.dataStore(dataStore);
            System.out.println(item.setup().toJson().toString());
        });
    }
}
