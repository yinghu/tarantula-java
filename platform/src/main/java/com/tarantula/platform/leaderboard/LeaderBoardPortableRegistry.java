package com.tarantula.platform.leaderboard;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;


public class LeaderBoardPortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 6;

    public static final int LEADER_BOARD_ENTRY_CID = 4;

    public static LeaderBoardPortableRegistry INS;

    public LeaderBoardPortableRegistry(){
        INS = this;
    }

    public T create(int i) {
        Recoverable pt = null;
        switch (i){
            case LEADER_BOARD_ENTRY_CID:
                pt = new LeaderBoardEntry();
                break;
            default:
        }
        return (T)pt;
    }

    public int registryId() {
        return OID;
    }
}
