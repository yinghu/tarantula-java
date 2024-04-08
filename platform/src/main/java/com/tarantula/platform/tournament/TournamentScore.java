package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.perfectday.games.earth8.BattleUpdate;
import com.perfectday.games.earth8.UnitXpUp;

public class TournamentScore extends RecoverableObject {
    private int score;

    public int score() {
        return score;
    }

    private long tournamentId;

    public long tournamentId() {
        return tournamentId;
    }

    public static TournamentScore fromJson(JsonObject jsonObject){
        TournamentScore self = new TournamentScore();
        self.score = JsonUtil.getJsonInt(jsonObject, "score", 0);
        self.tournamentId = JsonUtil.getJsonInt(jsonObject, "tournamentId", 0);
        return self;
    }
}
