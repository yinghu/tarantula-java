package com.icodesoftware.game.mahjong;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;

public class ClassicMahjong {
    private static final TarantulaLogger logger = JDKLogger.getLogger(ClassicMahjong.class);
    public static final HashMap<Integer,Tile> TILE_MAP = new HashMap<>(){
        {
            put(Tile.C1.rank,Tile.C1);
            put(Tile.C2.rank,Tile.C2);
            put(Tile.C3.rank,Tile.C3);
            put(Tile.C4.rank,Tile.C4);
            put(Tile.C5.rank,Tile.C5);
            put(Tile.C6.rank,Tile.C6);
            put(Tile.C7.rank,Tile.C7);
            put(Tile.C8.rank,Tile.C8);
            put(Tile.C9.rank,Tile.C9);

            put(Tile.B1.rank,Tile.B1);
            put(Tile.B2.rank,Tile.B2);
            put(Tile.B3.rank,Tile.B3);
            put(Tile.B4.rank,Tile.B4);
            put(Tile.B5.rank,Tile.B5);
            put(Tile.B6.rank,Tile.B6);
            put(Tile.B7.rank,Tile.B7);
            put(Tile.B8.rank,Tile.B8);
            put(Tile.B9.rank,Tile.B9);

            put(Tile.D1.rank,Tile.D1);
            put(Tile.D2.rank,Tile.D2);
            put(Tile.D3.rank,Tile.D3);
            put(Tile.D4.rank,Tile.D4);
            put(Tile.D5.rank,Tile.D5);
            put(Tile.D6.rank,Tile.D6);
            put(Tile.D7.rank,Tile.D7);
            put(Tile.D8.rank,Tile.D8);
            put(Tile.D9.rank,Tile.D9);
            //WIND
            put(Tile.W1.rank,Tile.W1);
            put(Tile.W2.rank,Tile.W2);
            put(Tile.W3.rank,Tile.W3);
            put(Tile.W4.rank,Tile.W4);
            put(Tile.W5.rank,Tile.W5);
            put(Tile.W6.rank,Tile.W6);
            put(Tile.W7.rank,Tile.W7);

            //FLOWER AND SEASON
            put(Tile.F1.rank,Tile.F1);
            put(Tile.F2.rank,Tile.F2);
            put(Tile.F3.rank,Tile.F3);
            put(Tile.F4.rank,Tile.F4);
            put(Tile.S1.rank,Tile.S1);
            put(Tile.S2.rank,Tile.S2);
            put(Tile.S3.rank,Tile.S3);
            put(Tile.S4.rank,Tile.S4);

        }
    };
    public static final short SHUFFLE = 1;
    public static final short START = 2;

    public static final short DRAW = 3;
    public static final short SWAP = 4;

    public static final short CLAIM = 5;



    public static boolean fourKind(Tile drop,Tile[] hand){
        int fourKind = 0;
        for(Tile t : hand){
            if(t.rank==drop.rank) fourKind++;
        }
        return fourKind==4;
    }
    public static boolean claim(Tile[] hand){
        ArrayBlockingQueue<Tile> characters = new ArrayBlockingQueue<>(14);
        ArrayBlockingQueue<Tile> bamboo = new ArrayBlockingQueue<>(14);
        ArrayBlockingQueue<Tile> dots = new ArrayBlockingQueue<>(14);
        ArrayBlockingQueue<Tile> honors = new ArrayBlockingQueue<>(14);

        return false;
    }


}
