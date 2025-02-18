package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.IntegerRangeKey;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class MatchMaking extends ResponseHeader {

    public static final IntegerRangeKey B0_100 = IntegerRangeKey.from(0,100);
    public static final IntegerRangeKey B101_200 = IntegerRangeKey.from(101,200);
    public static final IntegerRangeKey B201_300 = IntegerRangeKey.from(201,300);
    public static final IntegerRangeKey B301_400 = IntegerRangeKey.from(301,400);
    public static final IntegerRangeKey B401_500 = IntegerRangeKey.from(401,500);
    public static final IntegerRangeKey B501_600 = IntegerRangeKey.from(501,600);
    public static final IntegerRangeKey B601_700 = IntegerRangeKey.from(601,700);
    public static final IntegerRangeKey B701_800 = IntegerRangeKey.from(701,800);
    public static final IntegerRangeKey B801_900 = IntegerRangeKey.from(802,900);
    public static final IntegerRangeKey B901_1000 = IntegerRangeKey.from(901,1000);
    public static final IntegerRangeKey B1001_1100 = IntegerRangeKey.from(1001,1100);
    public static final IntegerRangeKey B1101_1200 = IntegerRangeKey.from(1101,1200);
    public static final IntegerRangeKey B1201_1300 = IntegerRangeKey.from(1201,1300);
    public static final IntegerRangeKey B1301_1400 = IntegerRangeKey.from(1301,1400);
    public static final IntegerRangeKey B1401_1500 = IntegerRangeKey.from(1401,1500);
    public static final IntegerRangeKey B1501_1600 = IntegerRangeKey.from(1501,1600);
    public static final IntegerRangeKey B1601_1700 = IntegerRangeKey.from(1601,1700);
    public static final IntegerRangeKey B1701_1800 = IntegerRangeKey.from(1701,1800);
    public static final IntegerRangeKey B1801_1900 = IntegerRangeKey.from(1801,1900);
    public static final IntegerRangeKey B1901_2000 = IntegerRangeKey.from(1901,2000);
    public static final IntegerRangeKey B2001_2100 = IntegerRangeKey.from(2001,2100);



    public long nextRefreshTime;
    public List<BattleTeam> battleTeams;

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
        return jsonObject;
    }

    public static MatchMaking success(long nextRefreshTime,List<BattleTeam> battleTeams){
        return new MatchMaking(nextRefreshTime,battleTeams);
    }

    public static MatchMaking failure(int code,String message){
        return new MatchMaking(code,message);
    }


    public static IntegerRangeKey rangedKey(int elo){
        return B0_100;
    }


}
