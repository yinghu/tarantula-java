package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TournamentRanking extends RecoverableObject implements Tournament.RaceBoard {
    private long tournamentId;

    private List<Tournament.Entry> topTenPlayers = new ArrayList<>();

    private List<Tournament.Entry> playerPersonalRankings = new ArrayList<>();

    public TournamentRanking(long tournamentId, List<Tournament.Entry> topTenPlayers, List<Tournament.Entry> playerPersonalRankings) {
        this.tournamentId = tournamentId;
        this.topTenPlayers = topTenPlayers;
        this.playerPersonalRankings = playerPersonalRankings;
    }


    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("TournamentId",tournamentId);

        JsonArray topTenPlayersJson = new JsonArray();
        topTenPlayers.forEach((v)->topTenPlayersJson.add(v.toJson()));
        jsonObject.add("_topTenPlayers", topTenPlayersJson);

        JsonArray playerPersonalRankingsJson = new JsonArray();
        playerPersonalRankings.forEach((v)->playerPersonalRankingsJson.add(v.toJson()));
        jsonObject.add("_playerPersonalRankings", playerPersonalRankingsJson);

        return jsonObject;
    }

    @Override
    public int size() {
        return topTenPlayers.size();
    }

    @Override
    public List<Tournament.Entry> list() {
        return topTenPlayers;
    }

    @Override
    public Tournament.Entry myPosition() {
        return null;
    }
}
