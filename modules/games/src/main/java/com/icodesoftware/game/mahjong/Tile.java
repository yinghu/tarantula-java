package com.icodesoftware.game.mahjong;


import java.util.Arrays;

public class Tile {

    //pong, kong, chow,

    //public enum Title { Dots, Bamboo, Characters, East, South, West, North, Red, Green, White, PlumBlossom,Orchid,Chrysanthemum}

    //Characters 36
    public final static Tile C1 = new Tile("C1",1);
    public final static Tile C2 = new Tile("C2",2);
    public final static Tile C3 = new Tile("C3",3);
    public final static Tile C4 = new Tile("C4",4);
    public final static Tile C5 = new Tile("C5",5);
    public final static Tile C6 = new Tile("C6",6);
    public final static Tile C7 = new Tile("C7",7);
    public final static Tile C8 = new Tile("C8",8);
    public final static Tile C9 = new Tile("C9",9);

    //Bamboo 36
    public final static Tile B1 = new Tile("B1",11);
    public final static Tile B2 = new Tile("B2",12);
    public final static Tile B3 = new Tile("B3",13);
    public final static Tile B4 = new Tile("B4",14);
    public final static Tile B5 = new Tile("B5",15);
    public final static Tile B6 = new Tile("B6",16);
    public final static Tile B7 = new Tile("B7",17);
    public final static Tile B8 = new Tile("B8",18);
    public final static Tile B9 = new Tile("B9",19);

    //Dots 36
    public final static Tile D1 = new Tile("D1",21); //*4
    public final static Tile D2 = new Tile("D2",22);
    public final static Tile D3 = new Tile("D3",23);
    public final static Tile D4 = new Tile("D4",24);
    public final static Tile D5 = new Tile("D5",25);
    public final static Tile D6 = new Tile("D6",26);
    public final static Tile D7 = new Tile("D7",27);
    public final static Tile D8 = new Tile("D8",28);
    public final static Tile D9 = new Tile("D9",29);


    //Winds And Dragons 28
    public final static Tile W1 = new Tile("East",31);
    public final static Tile W2 = new Tile("South",41);
    public final static Tile W3 = new Tile("West",51);
    public final static Tile W4 = new Tile("North",61);
    public final static Tile W5 = new Tile("Red",71);
    public final static Tile W6 = new Tile("Green",81);
    public final static Tile W7 = new Tile("White",91);

    //Flowers And Seasons 8
    public final static Tile F1 = new Tile("PlumBlossom",101);
    public final static Tile F2 = new Tile("Orchid",102);
    public final static Tile F3 = new Tile("Chrysanthemum",103);
    public final static Tile F4 = new Tile("Bamboo",104);

    public final static Tile S1 = new Tile("Spring",201);
    public final static Tile S2 = new Tile("Summer",202);

    public final static Tile S3 = new Tile("Autumn",203);
    public final static Tile S4 = new Tile("Winter",204);



    public final String name;
    public final int rank;

    public Tile(String name,int rank){
        this.name = name;
        this.rank = rank;
    }
    public String toString(){
        return "Name ["+name+"] Rank ["+rank+"]";
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(new int[]{rank});
    }
    @Override
    public boolean equals(Object obj){
        Tile r = (Tile) obj;
        return r.rank == this.rank;
    }
}
