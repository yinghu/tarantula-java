package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.IntegerKey;

import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.item.Application;

import java.util.List;

public class MatchMaking extends ResponseHeader {


    public long nextRefreshTime;
    public List<BattleTeam> battleTeams;
    public Application postBattleReward;

    private MatchMaking(int code,String message){
        this.code = code;
        this.message = message;
        this.successful = false;
    }

    private MatchMaking(long nextRefreshTime,List<BattleTeam> battleTeams){
        this.successful = true;
        this.nextRefreshTime = nextRefreshTime;
        this.battleTeams = battleTeams;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",successful);
        if(!successful){
            jsonObject.addProperty("ErrorCode",1);
            jsonObject.addProperty("Message",message);
            return jsonObject;
        }
        jsonObject.addProperty("NextRefreshTime",nextRefreshTime);
        JsonArray teams = new JsonArray();
        battleTeams.forEach(battleTeam -> teams.add(battleTeam.toJson()));
        jsonObject.add("_opponents",teams);
        if(postBattleReward!=null) jsonObject.add("_postBattleReward",postBattleReward.toJson());
        return jsonObject;
    }

    public static MatchMaking success(long nextRefreshTime,List<BattleTeam> battleTeams){
        return new MatchMaking(nextRefreshTime,battleTeams);
    }

    public static MatchMaking failure(int code,String message){
        return new MatchMaking(code,message);
    }


    public static IntegerKey pool(int poolNumber){
        return IntegerKey.from(poolNumber);
    }


}
