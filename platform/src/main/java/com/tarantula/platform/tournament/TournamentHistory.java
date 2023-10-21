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
        this.onEdge = true;
        this.label = Tournament.HISTORY_LABEL;
    }
    public TournamentHistory(String tournamentId,int rank,double score,LocalDateTime dateTime){
        this();
        this.tournamentId = tournamentId;
        this.rank = rank;
        this.score = score;
        this.dateTime = dateTime;
    }


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(tournamentId);
        buffer.writeInt(rank);
        buffer.writeDouble(score);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(dateTime));
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        tournamentId = buffer.readUTF8();
        rank = buffer.readInt();
        score = buffer.readDouble();
        dateTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        return true;
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
