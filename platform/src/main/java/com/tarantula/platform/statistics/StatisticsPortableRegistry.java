package com.tarantula.platform.statistics;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;

/**
 * updated by yinghu lu on 5/1/2020.
 */
public class StatisticsPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 5;


    public static final int STATISTICS_CID = 3;
    public static final int STATISTICS_ENTRY_CID = 5;
    public static final int STATISTICS_DELTA_CID = 7;


    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case STATISTICS_CID:
                pt = new StatisticsIndex();
                break;
            case STATISTICS_ENTRY_CID:
                pt = new StatisticsEntry();
                break;
            case STATISTICS_DELTA_CID:
                pt = new StatsDelta();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
