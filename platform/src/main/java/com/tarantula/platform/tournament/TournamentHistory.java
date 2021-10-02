package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TournamentHistory extends RecoverableObject implements Tournament.History {

    public String tournamentId;
    public int rank;
    public double score;
    public LocalDateTime dateTime;
    public TournamentHistory(){

    }
    public TournamentHistory(String tournamentId,int rank,double score,LocalDateTime dateTime){
        this.tournamentId = tournamentId;
        this.rank = rank;
        this.score = score;
        this.dateTime = dateTime;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("1",tournamentId);
        properties.put("2",rank);
        properties.put("3",score);
        properties.put("4", TimeUtil.toUTCMilliseconds(dateTime));
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.tournamentId = (String)properties.get("1");
        this.rank = ((Number)properties.get("2")).intValue();
        this.score = ((Number)properties.get("3")).doubleValue();
        this.dateTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("4")).longValue());
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_HISTORY_CID;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("tournamentId",tournamentId);
        jsonObject.addProperty("rank",rank);
        jsonObject.addProperty("score",score);
        jsonObject.addProperty("dataTime",dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        return jsonObject;
    }

    @Override
    public String tournamentId() {
        return tournamentId;
    }

    @Override
    public int rank() {
        return rank;
    }

    @Override
    public double score() {
        return score;
    }

    @Override
    public LocalDateTime dateTime() {
        return dateTime;
    }
}
