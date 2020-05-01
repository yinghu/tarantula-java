package com.tarantula.game;

public class Arena {
    public int level;
    public double xp;
    public String name;
    public int capacity;
    public int duration; //minutes
    public int playMode; //0,1,2
    public Arena(){}
    public Arena(int level,double xp,String name){
        this.level = level;
        this.xp = xp;
        this.name = name;
    }
    @Override
    public String toString(){
        return "["+name+"]["+level+"]["+xp+"]"+"["+capacity+"]["+duration+"]["+playMode+"]";
    }
}
