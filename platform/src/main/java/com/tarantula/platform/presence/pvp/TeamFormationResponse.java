package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.ResponseHeader;

import java.time.format.DateTimeFormatter;

public class TeamFormationResponse extends ResponseHeader {

    private boolean onOffense;

    private TeamFormationResponse(boolean successful,long nextUpdate,String message,boolean onOffense){
        this.successful = successful;
        this.timestamp = nextUpdate;
        this.message = message;
        this.onOffense = onOffense;
    }


    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("Successful",successful);
        if(onOffense){
            resp.addProperty("TeamId",timestamp);
        }
        else{
            resp.addProperty("NextUpdate", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
        }
        resp.addProperty("Message",message);
        return resp;
    }

    public static TeamFormationResponse success(long nextUpdate){
        return new TeamFormationResponse(true,nextUpdate,"saved",false);
    }
    public static TeamFormationResponse failure(long nextUpdate){
        return new TeamFormationResponse(false,nextUpdate,"not saved",false);
    }

    public static TeamFormationResponse responseOnOffenseTeam(long teamId){
        return new TeamFormationResponse(teamId>0,teamId,teamId>0? "saved":"not saved",true);
    }
}
