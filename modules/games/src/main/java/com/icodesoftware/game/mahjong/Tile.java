package com.icodesoftware.game.mahjong;


import java.util.Arrays;

public class Tile {

    //pong, kong, chow,

    //public enum Title { Dots, Bamboo, Characters, East, South, West, North, Red, Green, White, PlumBlossom,Orchid,Chrysanthemum}
    private static final int CHARACTER_SCORE = 10;
    private static final int BAMBOO_SCORE = 10;
    private static final int DOTS_SCORE = 10;

    private static final int WIND_SCORE = 100;
    private static final int DRAGON_SCORE = 150;

    private static final int FLOWER_SCORE = 100;

    private static final int SEASON_SCORE = 100;

    //Characters 36
    public final static Tile C1 = new Tile("C1",1,CHARACTER_SCORE);
    public final static Tile C2 = new Tile("C2",2,CHARACTER_SCORE);
    public final static Tile C3 = new Tile("C3",3,CHARACTER_SCORE);
    public final static Tile C4 = new Tile("C4",4,CHARACTER_SCORE);
    public final static Tile C5 = new Tile("C5",5,CHARACTER_SCORE);
    public final static Tile C6 = new Tile("C6",6,CHARACTER_SCORE);
    public final static Tile C7 = new Tile("C7",7,CHARACTER_SCORE);
    public final static Tile C8 = new Tile("C8",8,CHARACTER_SCORE);
    public final static Tile C9 = new Tile("C9",9,CHARACTER_SCORE);

    //Bamboo 36
    public final static Tile B1 = new Tile("B1",11,BAMBOO_SCORE);
    public final static Tile B2 = new Tile("B2",12,BAMBOO_SCORE);
    public final static Tile B3 = new Tile("B3",13,BAMBOO_SCORE);
    public final static Tile B4 = new Tile("B4",14,BAMBOO_SCORE);
    public final static Tile B5 = new Tile("B5",15,BAMBOO_SCORE);
    public final static Tile B6 = new Tile("B6",16,BAMBOO_SCORE);
    public final static Tile B7 = new Tile("B7",17,BAMBOO_SCORE);
    public final static Tile B8 = new Tile("B8",18,BAMBOO_SCORE);
    public final static Tile B9 = new Tile("B9",19,BAMBOO_SCORE);

    //Dots 36
    public final static Tile D1 = new Tile("D1",21,DOTS_SCORE); //*4
    public final static Tile D2 = new Tile("D2",22,DOTS_SCORE);
    public final static Tile D3 = new Tile("D3",23,DOTS_SCORE);
    public final static Tile D4 = new Tile("D4",24,DOTS_SCORE);
    public final static Tile D5 = new Tile("D5",25,DOTS_SCORE);
    public final static Tile D6 = new Tile("D6",26,DOTS_SCORE);
    public final static Tile D7 = new Tile("D7",27,DOTS_SCORE);
    public final static Tile D8 = new Tile("D8",28,DOTS_SCORE);
    public final static Tile D9 = new Tile("D9",29,DOTS_SCORE);


    //Winds And Dragons 28
    public final static Tile W1 = new Tile("East",31,WIND_SCORE);
    public final static Tile W2 = new Tile("South",41,WIND_SCORE);
    public final static Tile W3 = new Tile("West",51,WIND_SCORE);
    public final static Tile W4 = new Tile("North",61,WIND_SCORE);

    public final static Tile W5 = new Tile("Red",71,DRAGON_SCORE);
    public final static Tile W6 = new Tile("Green",81,DRAGON_SCORE);
    public final static Tile W7 = new Tile("White",91,DRAGON_SCORE);

    //Flowers And Seasons 8
    public final static Tile F1 = new Tile("PlumBlossom",101,FLOWER_SCORE);
    public final static Tile F2 = new Tile("Orchid",102,FLOWER_SCORE);
    public final static Tile F3 = new Tile("Chrysanthemum",103,FLOWER_SCORE);
    public final static Tile F4 = new Tile("Bamboo",104,FLOWER_SCORE);

    public final static Tile S1 = new Tile("Spring",201,SEASON_SCORE);
    public final static Tile S2 = new Tile("Summer",202,SEASON_SCORE);

    public final static Tile S3 = new Tile("Autumn",203,SEASON_SCORE);
    public final static Tile S4 = new Tile("Winter",204,SEASON_SCORE);



    public final String name;
    public final int rank;
    public final int score;

    public Tile(String name,int rank,int score){
        this.name = name;
        this.rank = rank;
        this.score = score;
    }
    public String toString(){
        return "Name :["+name+"] Rank :["+rank+"] Score :["+score+"]";
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(new int[]{rank,score});
    }
    @Override
    public boolean equals(Object obj){
        Tile r = (Tile) obj;
        return r.rank == this.rank;
    }
}
