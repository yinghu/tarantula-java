package com.tarantula.platform;

import com.tarantula.RecoverableFactory;
import com.tarantula.platform.service.cluster.PortableRegistry;

/**
 * Update by yinghu lu on 4/10/2019.
 */
public class StatisticsEntryQuery implements RecoverableFactory<StatisticsEntry> {

    private String statisticsId;

    public StatisticsEntryQuery(String onStateId){
        this.statisticsId = onStateId;
    }

    public String distributionKey() {
        return this.statisticsId;
    }


    public StatisticsEntry create() {
        StatisticsEntry se = new StatisticsEntry();
        //se.distributable(true);
        //se.index(SystemUtil.toString(new String[]{statisticsId,label()}));
        return se;
    }


    public int registryId() {
        return PortableRegistry.STATISTICS_ENTRY_CID;
    }

    public String label(){
        return "SSE";
    }
    public boolean onEdge(){
        return true;
    }
}
