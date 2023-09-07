package com.tarantula.admin;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.GameCluster;

public class GameClusterQuery implements RecoverableFactory<GameCluster> {


    private long accountId;

    public GameClusterQuery(long accountId){
        this.accountId = accountId;
    }
    @Override
    public GameCluster create() {
        return new GameCluster();
    }

    @Override
    public int registryId() {
        return 0;
    }

    @Override
    public String label() {
        return "gameCluster";
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(accountId);
    }
}
