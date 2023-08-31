package com.tarantula.platform.statistics;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;

public class StatisticsEntryQuery implements RecoverableFactory<StatisticsEntry> {


    private String ownerId;

    public StatisticsEntryQuery(String ownerId){
        this.ownerId = ownerId;
    }

    public StatisticsEntry create() {
        StatisticsEntry app = new StatisticsEntry();
        return app;
    }


    public String distributionKey() {
        return null;
    }

    public  int registryId(){
        return StatisticsPortableRegistry.STATISTICS_ENTRY_CID;
    }

    public String label(){
        return StatisticsEntry.LABEL;
    }

    @Override
    public Recoverable.Key key(){
        return new OidKey(ownerId);
    }
}
