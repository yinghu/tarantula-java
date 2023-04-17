package com.icodesoftware.game.mahjong;


import com.icodesoftware.service.RNG;
import com.icodesoftware.util.JvmRNG;

public class Stack {

    private static Tile[] tileList = new Tile[]{

            Tile.D1,Tile.D2,Tile.D3,Tile.D4,Tile.D5,Tile.D6,Tile.D7,Tile.D8,Tile.D9,
            Tile.D1,Tile.D2,Tile.D3,Tile.D4,Tile.D5,Tile.D6,Tile.D7,Tile.D8,Tile.D9,
            Tile.D1,Tile.D2,Tile.D3,Tile.D4,Tile.D5,Tile.D6,Tile.D7,Tile.D8,Tile.D9,
            Tile.D1,Tile.D2,Tile.D3,Tile.D4,Tile.D5,Tile.D6,Tile.D7,Tile.D8,Tile.D9,

            Tile.B1,Tile.B2,Tile.B3,Tile.B4,Tile.B5,Tile.B6,Tile.B7,Tile.B8,Tile.B9,
            Tile.B1,Tile.B2,Tile.B3,Tile.B4,Tile.B5,Tile.B6,Tile.B7,Tile.B8,Tile.B9,
            Tile.B1,Tile.B2,Tile.B3,Tile.B4,Tile.B5,Tile.B6,Tile.B7,Tile.B8,Tile.B9,
            Tile.B1,Tile.B2,Tile.B3,Tile.B4,Tile.B5,Tile.B6,Tile.B7,Tile.B8,Tile.B9,

            Tile.C1,Tile.C2,Tile.C3,Tile.C4,Tile.C5,Tile.C6,Tile.C7,Tile.C8,Tile.C9,
            Tile.C1,Tile.C2,Tile.C3,Tile.C4,Tile.C5,Tile.C6,Tile.C7,Tile.C8,Tile.C9,
            Tile.C1,Tile.C2,Tile.C3,Tile.C4,Tile.C5,Tile.C6,Tile.C7,Tile.C8,Tile.C9,
            Tile.C1,Tile.C2,Tile.C3,Tile.C4,Tile.C5,Tile.C6,Tile.C7,Tile.C8,Tile.C9,

            Tile.W1,Tile.W2,Tile.W3,Tile.W4,Tile.W5,Tile.W6,Tile.W7,
            Tile.W1,Tile.W2,Tile.W3,Tile.W4,Tile.W5,Tile.W6,Tile.W7,
            Tile.W1,Tile.W2,Tile.W3,Tile.W4,Tile.W5,Tile.W6,Tile.W7,
            Tile.W1,Tile.W2,Tile.W3,Tile.W4,Tile.W5,Tile.W6,Tile.W7,

            Tile.F1,Tile.F2,Tile.F3,Tile.F4,Tile.S1,Tile.S2,Tile.S3,Tile.S4
    };

    private final static int STACK_SIZE = 144;

    private int[] reel1;
    private int[] reel2;
    private int[] reel3;

    private RNG rnd;

    //private int cutter;
    private int stub;
    private int last;

    public Stack(){
        rnd = new JvmRNG();
        reel1 = new int[STACK_SIZE];
        init(reel1);
        reel2 = new int[STACK_SIZE];
        init(reel2);
        reel3 = new int[STACK_SIZE];
        init(reel3);
    }
    public void shuffle(){
        shuffle(reel1);
        shuffle(reel2);
        shuffle(reel3);
    }
    private void init(int[] reel){
        for(int i=0;i<reel.length;i++){
            reel[i]=i;
        }
    }
    private void shuffle(int[] reel){
        for (int i=STACK_SIZE-1;i>0;i--) {
            int _rx = rnd.onNext(i+1);
            int tmp = reel[_rx];
            reel[_rx] = reel[i];
            reel[i] = tmp;
        }
        //cutter = 144-rnd.onNext(13);
        this.stub = 0;
        this.last = STACK_SIZE-1;
    }

    public Tile[] swap(Tile[] tiles){
        if(tiles[0].sequence>100) tiles[0]=tileList[reel1[last]];
        if(tiles[1].sequence>100) tiles[1]=tileList[reel1[last]];
        if(tiles[2].sequence>100) tiles[2]=tileList[reel1[last]];
        last--;
        return tiles;
    }
    public Tile[] draw(){
        Tile[] tiles = new Tile[3];
        tiles[0] = tileList[reel1[stub]];
        tiles[1] = tileList[reel2[stub]];
        tiles[2] = tileList[reel3[stub++]];
        return tiles;
    }
}
