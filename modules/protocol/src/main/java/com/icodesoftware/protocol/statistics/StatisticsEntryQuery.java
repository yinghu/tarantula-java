package com.icodesoftware.protocol.statistics;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class StatisticsEntryQuery implements RecoverableFactory<StatisticsEntry> {


    private long ownerId;

    public StatisticsEntryQuery(long ownerId){
        this.ownerId = ownerId;
    }

    public StatisticsEntry create() {
        StatisticsEntry app = new StatisticsEntry();
        return app;
    }

    public String label(){
        return StatisticsEntry.LABEL;
    }

    @Override
    public Recoverable.Key key(){
        return new SnowflakeKey(ownerId);
    }
}
