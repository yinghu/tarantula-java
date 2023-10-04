package com.perfectday.games.earth8;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;


public class Earth8PortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 100;

    public static final int BATTLE_TRANSACTION_CID = 1;
    public static final int UNIT_XP_UP_CID = 2;

    public static Earth8PortableRegistry INS;

    public Earth8PortableRegistry(){
        INS = this;
    }

    public T create(int i) {
        Recoverable pt = null;
        switch (i){
            case BATTLE_TRANSACTION_CID:
                pt = new BattleTransaction();
                break;
            case UNIT_XP_UP_CID:
                pt = new UnitXpUp();
                break;
            default:
        }
        return (T)pt;
    }

    public int registryId() {
        return OID;
    }
}
