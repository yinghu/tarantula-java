package com.tarantula.game;

/**
 * Created by yinghu lu on 3/20/2019.
 */
public class GameStatisticsEntry extends GameComponent {

    public String key;
    public double value;

    public GameStatisticsEntry(String name,String label,String key,double value,int index){
        this.name = name;
        this.label = label;
        this.key = key;
        this.value = value;
        this.subscript = index;
    }
}
