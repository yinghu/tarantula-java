package com.tarantula.platform.statistics;

import com.icodesoftware.util.RecoverableObject;


public class StatsDelta extends RecoverableObject {
    public String name;
    public double value;

    public StatsDelta(){

    }
    public StatsDelta(String name,double value){
        this.name = name;
        this.value = value;
    }

    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.STATISTICS_DELTA_CID;
    }
}
