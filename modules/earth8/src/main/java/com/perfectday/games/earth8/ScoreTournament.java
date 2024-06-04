package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class ScoreTournament extends BattleUpdate {
    @Override
    public int getClassId() {
        return Earth8PortableRegistry.SCORE_TOURNAMENT_CID;
    }

    public static ScoreTournament fromJson(JsonObject jsonObject) {
        ScoreTournament scoreTournament = new ScoreTournament();
        scoreTournament.parse(jsonObject);
        return scoreTournament;
    }
}
