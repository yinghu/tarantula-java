package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.BattleUpdate;

public class BattleStartTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/battleStart";

    public BattleStartTransaction(Session session, long battleId, byte[] clientData)
    {
        super(MESSAGE_TYPE, session);
        var object = JsonUtil.parse(clientData);
        data.addProperty("battle_id", battleId);
        data.add("chapter_id", object.get("TEMP_ChapterName"));
        data.add("stage_id", object.get("TEMP_StageName"));
        data.add("party", object.get("TEMP_StringParty"));
        data.add("difficulty", object.get("Difficulty"));
        data.add("autobattle", object.get("AutoBattle"));

    }
}
