package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.analytics.UnitLevelUpTransaction;
import com.perfectday.games.earth8.analytics.UnitXpUpTransaction;

import static com.perfectday.games.earth8.Earth8GameServiceProvider.tournamentIndex;

public class TournamentScore extends BattleUpdate{

    public int score;

    public long tournamentId;

    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeInt(score);
        buffer.writeLong(tournamentId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        score = buffer.readInt();
        tournamentId = buffer.readLong();
        return true;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.TOURNAMENT_SCORE_CID;
    }


    public static TournamentScore fromJson(JsonObject jsonObject){
        TournamentScore self = new TournamentScore();
        self.parse(jsonObject);
        self.score = JsonUtil.getJsonInt(jsonObject, "Score", 0);
        self.tournamentId = JsonUtil.getJsonLong(jsonObject, "TournamentId", 0);
        return self;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        var tournament = tournamentIndex.getOrDefault(tournamentId, null);
        if (tournament != null && tournament.status() == Tournament.Status.STARTED) {
            tournament.register(session).update(session,(e)->{
                e.score(0,score);
                return true;
            });
        }
        return true;
    }
}
