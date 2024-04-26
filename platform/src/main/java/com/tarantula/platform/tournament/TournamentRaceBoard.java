package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.icodesoftware.Tournament;

import com.icodesoftware.util.RecoverableObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TournamentRaceBoard extends RecoverableObject implements Tournament.RaceBoard {

    private static final int ENTRY_DATA_SIZE = 28;

    private final List<Tournament.Entry> snapshot;

    public TournamentRaceBoard(List<Tournament.Entry> snapshot){
        this.snapshot = snapshot;
    }

    public TournamentRaceBoard(){
        this.snapshot = new ArrayList<>();
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_RACE_BOARD_CID;
    }
    @Override
    public int size() {
        return snapshot.size();
    }

    @Override
    public List<Tournament.Entry> list() {
        return snapshot;
    }



    public Tournament.Entry myPosition(){
        return null;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        JsonArray plist = new JsonArray();
        int[] rank = {1};
        snapshot.forEach((v)->{
            ((TournamentEntry)v).rank(rank[0]++);
            plist.add(v.toJson());
        });
        jsonObject.add("_board",plist);
        return jsonObject;
    }


    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(snapshot.size()*ENTRY_DATA_SIZE);
        snapshot.forEach(entry -> {
            buffer.putLong(entry.systemId());
            buffer.putDouble(entry.score());
            buffer.putLong(entry.timestamp());
            buffer.putInt(entry.rank());
        });
        return buffer.array();
    }

    @Override
    public void fromBinary(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        while (buffer.hasRemaining()){
            snapshot.add(TournamentEntry.from(buffer.getLong(),buffer.getDouble(),buffer.getLong(),buffer.getInt()));
        }
    }

    public static TournamentRaceBoard from(byte[] payload){
        TournamentRaceBoard tournamentRaceBoard = new TournamentRaceBoard();
        tournamentRaceBoard.fromBinary(payload);
        return tournamentRaceBoard;
    }
}
