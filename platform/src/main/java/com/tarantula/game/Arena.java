package com.tarantula.game;

public class Arena {
    public int level;
    public double xp;
    public String name;
    public int capacity;
    public int duration; //minutes
    public int playMode; //0,1,2
    public Arena(){}
    public Arena(int level,double xp,String name,int capacity,int duration,int playMode){
        this.level = level;
        this.xp = xp;
        this.name = name;
        this.capacity = capacity;
        this.duration = duration;
        this.playMode = playMode;
    }
    @Override
    public String toString(){
        return "["+name+"]["+level+"]["+xp+"]"+"["+capacity+"]["+duration+"]["+playMode+"]";
    }
}
