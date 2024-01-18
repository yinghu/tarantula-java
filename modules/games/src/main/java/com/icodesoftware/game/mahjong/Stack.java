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
    private final static int MAX_REEL_SIZE = 3;
    private final static int MIN_REEL_SIZE = 1;


    private static final int[][] reels = new int[MAX_REEL_SIZE][];
    private static final int[][] index = new int[MAX_REEL_SIZE][];

    private final RNG rnd;

    //private int cutter;

    private final int size;
    private int stub;
    private int last;

    private Stack(int size){
        this.size = size;
        rnd = new JvmRNG();
        for(int i=0;i<reels.length;i++){
            reels[i]=new int[STACK_SIZE];
            index[i]= new int[]{0,0};
            init(reels[i]);
        }
    }
    public void shuffle(){
        for(int i=0;i<size;i++){
            shuffle(reels[i]);
        }
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
        this.stub = 0;
        this.last = STACK_SIZE-1;
    }

    public Tile[] swap(Tile[] tiles){
        if(tiles[0].rank>100) tiles[0]=tileList[reels[0][last]];
        if(tiles[1].rank>100) tiles[1]=tileList[reels[1][last]];
        if(tiles[2].rank>100) tiles[2]=tileList[reels[2][last]];
        last--;
        return tiles;
    }
    public Tile[] draw(){
        Tile[] tiles = new Tile[size];
        int ix = 0;
        for(int i=0;i<size;i++){
            tiles[ix++]=tileList[reels[i][stub]];
        }
        stub++;
        return tiles;
    }
    public void draw(Tile[] slots){
        for(int i=0;i<slots.length;i++){
            slots[i]=tileList[reels[0][stub++]];
        }
    }

    public static Stack stack(int reels){
        int size = reels;
        if(reels<MIN_REEL_SIZE) size = MIN_REEL_SIZE;
        if(reels>MAX_REEL_SIZE) size = MAX_REEL_SIZE;
        Stack stack = new Stack(size);
        return stack;
    }
}
