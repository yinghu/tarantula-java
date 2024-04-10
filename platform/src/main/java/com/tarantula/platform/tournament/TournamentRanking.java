package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Presence;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.presence.Profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TournamentRanking extends RecoverableObject implements Portable {

    private List<Tournament.Entry> topTenPlayers = new ArrayList<>();

    private List<Tournament.Entry> playerPersonalRankings = new ArrayList<>();

    private long playerID;

    public TournamentRanking() {

    }
    public TournamentRanking(Tournament.RaceBoard board, long playerID) {
        this.playerID = playerID;
        getData(board);
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {

    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {

    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();

        JsonArray topTenPlayersJson = new JsonArray();
        topTenPlayers.forEach((v)->topTenPlayersJson.add(v.toJson()));
        jsonObject.add("_topTenPlayers", topTenPlayersJson);

        JsonArray playerPersonalRankingsJson = new JsonArray();
        playerPersonalRankings.forEach((v)->playerPersonalRankingsJson.add(v.toJson()));
        jsonObject.add("_playerPersonalRankings", playerPersonalRankingsJson);

        return jsonObject;
    }

    private void getData(Tournament.RaceBoard board){
        List<Tournament.Entry> leaderBoard = board.list();

        //Add top 10
        for(int i = 0; i < 10; i++){
            if(i < leaderBoard.size()){
                Tournament.Entry otherPlayer = leaderBoard.get(i);
                topTenPlayers.add(otherPlayer);
            }
        }

        //Get player index
        int playerIndex = 0;

        for(int i = 0; i < leaderBoard.size(); i++){
            if(leaderBoard.get(i).systemId() == playerID){
                playerIndex = i;
            }
        }

        //Add Player
        Tournament.Entry player = leaderBoard.get(playerIndex);
        playerPersonalRankings.add(leaderBoard.get(playerIndex));

        // Add 5 in front
        for(int i = 1; i < 6; i++){
            if(!(playerIndex - i < 0)){
                Tournament.Entry otherPlayer = leaderBoard.get(playerIndex - i);
                playerPersonalRankings.addFirst(otherPlayer);
            }
        }

        //Add 1 behind
        if(leaderBoard.size() > playerIndex + 1){
            Tournament.Entry otherPlayer = leaderBoard.get(playerIndex + 1);
            playerPersonalRankings.add(leaderBoard.get(playerIndex + 1));
        }

    }
}
