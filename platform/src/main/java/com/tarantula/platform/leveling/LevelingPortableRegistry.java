package com.tarantula.platform.leveling;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;
import com.tarantula.platform.OnStatisticsTrack;

/**
 * Created by yinghu lu on 3/31/2018.
 */
public class LevelingPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 4;

    public static final int LEVEL_CID = 1;
    public static final int XP_CID = 2;
    public static final int ON_STATS_CID = 3;
    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case LEVEL_CID:
                pt = new XPLevel();
                break;
            case XP_CID:
                pt = new XPGain();
                break;
            case ON_STATS_CID:
                pt = new OnStatisticsTrack();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }

}
