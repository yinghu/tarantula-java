package com.icodesoftware.game.mahjong;


import java.util.Arrays;

public class Tile {

    //pong, kong, chow,

    public enum Category { Dots, Bamboo, Characters, Honors, Flowers, Seasons }
    private static final int CHARACTER_SCORE = 10;
    private static final int BAMBOO_SCORE = 10;
    private static final int DOTS_SCORE = 10;

    private static final int WIND_SCORE = 100;
    private static final int DRAGON_SCORE = 150;

    private static final int FLOWER_SCORE = 100;

    private static final int SEASON_SCORE = 100;

    //Characters 36
    public final static Tile C1 = new Tile(Category.Characters,"C1",1,CHARACTER_SCORE);
    public final static Tile C2 = new Tile(Category.Characters,"C2",2,CHARACTER_SCORE);
    public final static Tile C3 = new Tile(Category.Characters,"C3",3,CHARACTER_SCORE);
    public final static Tile C4 = new Tile(Category.Characters,"C4",4,CHARACTER_SCORE);
    public final static Tile C5 = new Tile(Category.Characters,"C5",5,CHARACTER_SCORE);
    public final static Tile C6 = new Tile(Category.Characters,"C6",6,CHARACTER_SCORE);
    public final static Tile C7 = new Tile(Category.Characters,"C7",7,CHARACTER_SCORE);
    public final static Tile C8 = new Tile(Category.Characters,"C8",8,CHARACTER_SCORE);
    public final static Tile C9 = new Tile(Category.Characters,"C9",9,CHARACTER_SCORE);

    //Bamboo 36
    public final static Tile B1 = new Tile(Category.Bamboo,"B1",11,BAMBOO_SCORE);
    public final static Tile B2 = new Tile(Category.Bamboo,"B2",12,BAMBOO_SCORE);
    public final static Tile B3 = new Tile(Category.Bamboo,"B3",13,BAMBOO_SCORE);
    public final static Tile B4 = new Tile(Category.Bamboo,"B4",14,BAMBOO_SCORE);
    public final static Tile B5 = new Tile(Category.Bamboo,"B5",15,BAMBOO_SCORE);
    public final static Tile B6 = new Tile(Category.Bamboo,"B6",16,BAMBOO_SCORE);
    public final static Tile B7 = new Tile(Category.Bamboo,"B7",17,BAMBOO_SCORE);
    public final static Tile B8 = new Tile(Category.Bamboo,"B8",18,BAMBOO_SCORE);
    public final static Tile B9 = new Tile(Category.Bamboo,"B9",19,BAMBOO_SCORE);

    //Dots 36
    public final static Tile D1 = new Tile(Category.Dots,"D1",21,DOTS_SCORE); //*4
    public final static Tile D2 = new Tile(Category.Dots,"D2",22,DOTS_SCORE);
    public final static Tile D3 = new Tile(Category.Dots,"D3",23,DOTS_SCORE);
    public final static Tile D4 = new Tile(Category.Dots,"D4",24,DOTS_SCORE);
    public final static Tile D5 = new Tile(Category.Dots,"D5",25,DOTS_SCORE);
    public final static Tile D6 = new Tile(Category.Dots,"D6",26,DOTS_SCORE);
    public final static Tile D7 = new Tile(Category.Dots,"D7",27,DOTS_SCORE);
    public final static Tile D8 = new Tile(Category.Dots,"D8",28,DOTS_SCORE);
    public final static Tile D9 = new Tile(Category.Dots,"D9",29,DOTS_SCORE);


    //Winds And Dragons 28
    public final static Tile W1 = new Tile(Category.Honors,"East",31,WIND_SCORE);
    public final static Tile W2 = new Tile(Category.Honors,"South",41,WIND_SCORE);
    public final static Tile W3 = new Tile(Category.Honors,"West",51,WIND_SCORE);
    public final static Tile W4 = new Tile(Category.Honors,"North",61,WIND_SCORE);

    public final static Tile W5 = new Tile(Category.Honors,"Red",71,DRAGON_SCORE);
    public final static Tile W6 = new Tile(Category.Honors,"Green",81,DRAGON_SCORE);
    public final static Tile W7 = new Tile(Category.Honors,"White",91,DRAGON_SCORE);

    //Flowers And Seasons 8
    public final static Tile F1 = new Tile(Category.Flowers,"PlumBlossom",101,FLOWER_SCORE,true);
    public final static Tile F2 = new Tile(Category.Flowers,"Orchid",103,FLOWER_SCORE,true);
    public final static Tile F3 = new Tile(Category.Flowers,"Chrysanthemum",105,FLOWER_SCORE,true);
    public final static Tile F4 = new Tile(Category.Flowers,"Bamboo",107,FLOWER_SCORE,true);

    public final static Tile S1 = new Tile(Category.Seasons,"Spring",201,SEASON_SCORE,true);
    public final static Tile S2 = new Tile(Category.Seasons,"Summer",203,SEASON_SCORE,true);

    public final static Tile S3 = new Tile(Category.Seasons,"Autumn",205,SEASON_SCORE,true);
    public final static Tile S4 = new Tile(Category.Seasons,"Winter",207,SEASON_SCORE,true);


    public final Category category;
    public final String name;

    public final int rank;
    public final int score;
    public boolean swappable;

    public Tile(Category category,String name,int rank,int score,boolean swappable){
        this.category = category;
        this.name = name;
        this.rank = rank;
        this.score = score;
        this.swappable = swappable;
    }

    public Tile(Category category,String name,int rank,int score){
        this.category = category;
        this.name = name;
        this.rank = rank;
        this.score = score;
        this.swappable = false;
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
