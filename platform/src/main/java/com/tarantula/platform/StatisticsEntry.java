package com.tarantula.platform;


import com.tarantula.Statistics;


/**
 * Updated by yinghu lu on 4/29/2020
 */
public class StatisticsEntry implements Statistics.Entry {

    private String name;
    private double value=0;

    public StatisticsEntry(String name){
        this.name = name;
    }
    public StatisticsEntry(String name,double value){
        this(name);
        this.value = value;
    }
    @Override
    public String name() {
        return name;
    }
    @Override
    public double value() {
        return this.value;
    }
    @Override
    public void value(double delta) {
        this.value = this.value+delta;
    }

    @Override
    public String toString(){
        return name+"/"+value;
    }
}
