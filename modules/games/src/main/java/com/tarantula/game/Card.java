package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 8/21/2019
 */
public class Card extends GameObject {

    public enum Suit {Spades,Hearts,Diamonds,Clubs,Back};

    public static Card S1 = new Card(Suit.Spades,"A",1,0);
    public static Card S2 = new Card(Suit.Spades,"2",2,1);
    public static Card S3 = new Card(Suit.Spades,"3",3,2);
    public static Card S4 = new Card(Suit.Spades,"4",4,3);
    public static Card S5 = new Card(Suit.Spades,"5",5,4);
    public static Card S6 = new Card(Suit.Spades,"6",6,5);
    public static Card S7 = new Card(Suit.Spades,"7",7,6);
    public static Card S8 = new Card(Suit.Spades,"8",8,7);
    public static Card S9 = new Card(Suit.Spades,"9",9,8);
    public static Card S10 = new Card(Suit.Spades,"10",10,9);
    public static Card S11 = new Card(Suit.Spades,"J",10,10);
    public static Card S12 = new Card(Suit.Spades,"Q",10,11);
    public static Card S13 = new Card(Suit.Spades,"K",10,12);

    public static Card H1 = new Card(Suit.Hearts,"A",1,13);
    public static Card H2 = new Card(Suit.Hearts,"2",2,14);
    public static Card H3 = new Card(Suit.Hearts,"3",3,15);
    public static Card H4 = new Card(Suit.Hearts,"4",4,16);
    public static Card H5 = new Card(Suit.Hearts,"5",5,17);
    public static Card H6 = new Card(Suit.Hearts,"6",6,18);
    public static Card H7 = new Card(Suit.Hearts,"7",7,19);
    public static Card H8 = new Card(Suit.Hearts,"8",8,20);
    public static Card H9 = new Card(Suit.Hearts,"9",9,21);
    public static Card H10 = new Card(Suit.Hearts,"10",10,22);
    public static Card H11 = new Card(Suit.Hearts,"J",10,23);
    public static Card H12 = new Card(Suit.Hearts,"Q",10,24);
    public static Card H13 = new Card(Suit.Hearts,"K",10,25);

    public static Card D1 = new Card(Suit.Diamonds,"A",1,26);
    public static Card D2 = new Card(Suit.Diamonds,"2",2,27);
    public static Card D3 = new Card(Suit.Diamonds,"3",3,28);
    public static Card D4 = new Card(Suit.Diamonds,"4",4,29);
    public static Card D5 = new Card(Suit.Diamonds,"5",5,30);
    public static Card D6 = new Card(Suit.Diamonds,"6",6,31);
    public static Card D7 = new Card(Suit.Diamonds,"7",7,32);
    public static Card D8 = new Card(Suit.Diamonds,"8",8,33);
    public static Card D9 = new Card(Suit.Diamonds,"9",9,34);
    public static Card D10 = new Card(Suit.Diamonds,"10",10,35);
    public static Card D11 = new Card(Suit.Diamonds,"J",10,36);
    public static Card D12 = new Card(Suit.Diamonds,"Q",10,37);
    public static Card D13 = new Card(Suit.Diamonds,"K",10,38);

    public static Card C1 = new Card(Suit.Clubs,"A",1,39);
    public static Card C2 = new Card(Suit.Clubs,"2",2,40);
    public static Card C3 = new Card(Suit.Clubs,"3",3,41);
    public static Card C4 = new Card(Suit.Clubs,"4",4,42);
    public static Card C5 = new Card(Suit.Clubs,"5",5,43);
    public static Card C6 = new Card(Suit.Clubs,"6",6,44);
    public static Card C7 = new Card(Suit.Clubs,"7",7,45);
    public static Card C8 = new Card(Suit.Clubs,"8",8,46);
    public static Card C9 = new Card(Suit.Clubs,"9",9,47);
    public static Card C10 = new Card(Suit.Clubs,"10",10,48);
    public static Card C11 = new Card(Suit.Clubs,"J",10,49);
    public static Card C12 = new Card(Suit.Clubs,"Q",10,50);
    public static Card C13 = new Card(Suit.Clubs,"K",10,51);

    public static Card BJ = new Card(Suit.Back,"",0,52);

    public Suit suit;
    public int rank;
    public int sequence;

    public Card(Suit suit, String name, int rank, int sequence){
        this.suit = suit;
        this.name = name;
        this.rank = rank;
        this.sequence = sequence;
    }
    @Override
    public int hashCode(){
        return this.suit.hashCode()+this.name.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        Card r = (Card) obj;
        return r.sequence == this.sequence;
    }
    @Override
    public String toString(){
        return "["+suit+"/"+name+"]["+rank+"]["+sequence+"]";
    }
    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jo = new JsonObject();
        jo.addProperty("name",this.name);
        jo.addProperty("suit",this.suit.name());
        jo.addProperty("rank",this.rank);
        jo.addProperty("sequence",this.sequence);
        return jo;
    }
}