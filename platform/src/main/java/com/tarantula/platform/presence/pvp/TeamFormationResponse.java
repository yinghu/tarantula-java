package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.ResponseHeader;

import java.time.format.DateTimeFormatter;

public class TeamFormationResponse extends ResponseHeader {

    private boolean onOffense;

    private TeamFormationResponse(int code,String message,boolean onOffense){
        this.successful = false;
        this.code = code;
        this.message = message;
        this.onOffense = onOffense;
    }
    private TeamFormationResponse(int code,long nextUpdate){
        this.successful = false;
        this.code = code;
        this.timestamp = nextUpdate;
        this.onOffense = false;
        this.message = "defense team formation time limit";
    }

    private TeamFormationResponse(long value,boolean onOffense){
        this.successful = true;
        this.onOffense = onOffense;
        this.timestamp = value;
        this.message = onOffense?"offense team formation":"defense team formation";
    }


    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("Successful",successful);
        if(!successful){
            resp.addProperty("ErrorCode",code);
            resp.addProperty("Message",message);
            if(!onOffense){
                resp.addProperty("NextUpdate", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
            }
            return resp;
        }
        if(onOffense){
            resp.addProperty("TeamId",timestamp);
        }
        else{
            resp.addProperty("NextUpdate", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
        }
        resp.addProperty("Message",message);
        return resp;
    }

    public static TeamFormationResponse responseOnDefenseTeam(long nextUpdate){
        return new TeamFormationResponse(nextUpdate,false);
    }

    public static TeamFormationResponse failureOnDefenseTeam(long nextUpdate){
        return new TeamFormationResponse(PvpErrorCode.TEAM_FORMATION_TIME_LIMIT,nextUpdate);
    }

    public static TeamFormationResponse retryOffenseTeam(){
        return new TeamFormationResponse(PvpErrorCode.OFFENSE_TEAM_FORMATION_RETRY,"retry offense team formation",true);
    }

    public static TeamFormationResponse responseOnOffenseTeam(long teamId){
        return new TeamFormationResponse(teamId,true);
    }
}
