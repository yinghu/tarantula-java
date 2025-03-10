package com.tarantula.platform.presence.pvp;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;


public class BotFormationQuery implements RecoverableFactory<BattleTeam> {

    private long nodeId;

    public BotFormationQuery(long nodeId){
        this.nodeId = nodeId;
    }
    @Override
    public BattleTeam create() {
        return new BattleTeam();
    }

    @Override
    public String label() {
        return "defense_bot";
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(nodeId);
    }
}
