package com.tarantula.game.mahjong;

public class Tile {

    //Dots 36
    public final static Tile D1 = new Tile("",1); //*4
    public final static Tile D2 = new Tile("",2);
    public final static Tile D3 = new Tile("",3);
    public final static Tile D4 = new Tile("",4);
    public final static Tile D5 = new Tile("",5);
    public final static Tile D6 = new Tile("",6);
    public final static Tile D7 = new Tile("",7);
    public final static Tile D8 = new Tile("",8);
    public final static Tile D9 = new Tile("",9);

    //Bamboo 36
    public final static Tile B1 = new Tile("",1);
    public final static Tile B2 = new Tile("",2);
    public final static Tile B3 = new Tile("",3);
    public final static Tile B4 = new Tile("",4);
    public final static Tile B5 = new Tile("",5);
    public final static Tile B6 = new Tile("",6);
    public final static Tile B7 = new Tile("",7);
    public final static Tile B8 = new Tile("",8);
    public final static Tile B9 = new Tile("",9);

    //Characters 36
    public final static Tile C1 = new Tile("",1);
    public final static Tile C2 = new Tile("",2);
    public final static Tile C3 = new Tile("",3);
    public final static Tile C4 = new Tile("",4);
    public final static Tile C5 = new Tile("",5);
    public final static Tile C6 = new Tile("",6);
    public final static Tile C7 = new Tile("",7);
    public final static Tile C8 = new Tile("",8);
    public final static Tile C9 = new Tile("",9);

    //Winds And Dragons 28
    public final static Tile W1 = new Tile("",1);
    public final static Tile W2 = new Tile("",2);
    public final static Tile W3 = new Tile("",3);
    public final static Tile W4 = new Tile("",4);
    public final static Tile W5 = new Tile("",5);
    public final static Tile W6 = new Tile("",6);
    public final static Tile W7 = new Tile("",7);

    //Flowers And Seasons 8
    public final static Tile F1 = new Tile("",8);
    public final static Tile F2 = new Tile("",9);
    public final static Tile F3 = new Tile("",8);
    public final static Tile F4 = new Tile("",9);

    public final static Tile F5 = new Tile("",8);
    public final static Tile F6 = new Tile("",9);
    public final static Tile F7 = new Tile("",8);
    public final static Tile F8 = new Tile("",9);



    public final String name;
    public final int rank;

    public Tile(String name,int rank){
        this.name = name;
        this.rank = rank;
    }
}
