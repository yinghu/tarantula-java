package com.tarantula.platform.statistics;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.service.metrics.*;

public class StatisticsPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 5;


    public static final int STATISTICS_CID = 3;
    public static final int STATISTICS_ENTRY_CID = 5;
    public static final int STATISTICS_DELTA_CID = 7;

    public static final int SYSTEM_STATISTICS_ENTRY_CID = 8;
    public static final int SYSTEM_STATISTICS_CID = 9;

    public static final int METRICS_PROPERTY_CID= 11;


    public static final int METRICS_SNAPSHOT_CID = 16;


    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case STATISTICS_CID:
                pt = new UserStatistics();
                break;
            case STATISTICS_ENTRY_CID:
                pt = new StatisticsEntry();
                break;
            case STATISTICS_DELTA_CID:
                pt = new StatsDelta();
                break;
            case SYSTEM_STATISTICS_ENTRY_CID:
                pt = new SystemStatisticsEntry();
                break;
            case SYSTEM_STATISTICS_CID:
                pt = new SystemStatistics();
                break;
            case METRICS_PROPERTY_CID:
                pt = new MetricsProperty();
                break;
            case METRICS_SNAPSHOT_CID:
                pt = new MetricsSnapshot();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
