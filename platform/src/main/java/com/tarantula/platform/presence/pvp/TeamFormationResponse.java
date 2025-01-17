package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.ResponseHeader;

import java.time.format.DateTimeFormatter;

public class TeamFormationResponse extends ResponseHeader {

    private TeamFormationResponse(boolean successful,long nextUpdate,String message){
        this.successful = successful;
        this.timestamp = nextUpdate;
        this.message = message;
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("Successful",successful);
        resp.addProperty("NextUpdate", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
        resp.addProperty("Message",message);
        return resp;
    }

    public static TeamFormationResponse success(long nextUpdate){
        return new TeamFormationResponse(true,nextUpdate,"saved");
    }
    public static TeamFormationResponse failure(long nextUpdate){
        return new TeamFormationResponse(false,nextUpdate,"not saved");
    }
}
