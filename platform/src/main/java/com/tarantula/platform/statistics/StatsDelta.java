package com.tarantula.platform.statistics;

public class StatsDelta {
    public final String name;
    public final double value;

    public StatsDelta(String name,double value){
        this.name = name;
        this.value = value;
    }
}
