package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

public class TournamentScheduleParser {
    public static Tournament.Schedule parse(byte[] payload){
        InputStreamReader inr = new InputStreamReader(new ByteArrayInputStream(payload));
        JsonObject cmd = new JsonParser().parse(inr).getAsJsonObject();
        String type = cmd.get("type").getAsString();
        String name = cmd.get("name").getAsString();
        String _schedule = Tournament.DAILY_SCHEDULE;
        if(cmd.has("schedule")){
            _schedule = cmd.get("schedule").getAsString();
        }
        DefaultTournamentSchedule schedule;
        if(_schedule.equals(Tournament.ON_DEMAND_SCHEDULE)){
            LocalDateTime _start = LocalDateTime.now().plusMinutes(5);//delay 5 minutes to start
            long hours = cmd.get("durationHours").getAsLong();
            int minutes = cmd.get("durationMinutesPerInstance").getAsInt();
            int entries = cmd.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(hours*60-minutes);
            LocalDateTime _end  = _start.plusHours(hours);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.ON_DEMAND_SCHEDULE,_start,_close,_end,minutes,entries);

        }
        else if(_schedule.equals(Tournament.DAILY_SCHEDULE)){
            LocalDateTime _start = TimeUtil.midnight();
            int minutes = cmd.get("durationMinutesPerInstance").getAsInt();
            int entries = cmd.get("maxEntriesPerInstance").getAsInt();
            LocalDateTime _close = _start.plusMinutes(24*60-minutes);
            LocalDateTime _end  = _start.plusHours(24);
            schedule = new DefaultTournamentSchedule(type,name,Tournament.DAILY_SCHEDULE,_start,_close,_end,minutes,entries);
        }
        else{
            throw new UnsupportedOperationException(_schedule+" not supported");
        }
        return schedule;
    }
}
