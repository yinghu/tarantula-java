package com.perfectday.games.earth8.data;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.RecoverableObject;
import com.perfectday.games.earth8.Earth8PortableRegistry;

public class PlayerDataTrack extends RecoverableObject {

    public static final String LABEL = "tournament";

    public long tournamentId;


    public PlayerDataTrack(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public PlayerDataTrack(long tournamentId){
        this();
        this.tournamentId = tournamentId;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.PLAYER_TOURNAMENT_TRACK_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(tournamentId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        tournamentId = buffer.readLong();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("TournamentId",tournamentId);
        return jsonObject;
    }

}
