package com.perfectday.games.earth8;

import com.icodesoftware.Session;
import com.perfectday.games.earth8.BattleTransaction;

public class PendingBattle {
    public final BattleTransaction battleTransaction;
    public final Session session;

    public PendingBattle(Session session, BattleTransaction battleTransaction){
        this.session = session;
        this.battleTransaction = battleTransaction;
    }
}
