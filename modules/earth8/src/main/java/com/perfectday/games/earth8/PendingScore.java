package com.perfectday.games.earth8;

import com.icodesoftware.Session;
import com.perfectday.games.earth8.data.PlayerDataTrack;

public class PendingScore {

    public final BattleUpdate battleUpdate;
    public final PlayerDataTrack serverSession;
    public final Session session;
    public PendingScore(Session session,PlayerDataTrack serverSession,BattleUpdate battleUpdate){
        this.session = session;
        this.serverSession = serverSession;
        this.battleUpdate = battleUpdate;
    }
}
