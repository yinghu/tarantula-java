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

    private final RNG rnd;

    private final int size;
    private int reelIndex;
    private int[] stackIndex;
    private int[] stackCutter;


    private Stack(int size){
        this.size = size;
        rnd = new JvmRNG();
        for(int i=0;i<reels.length;i++){
            reels[i]=new int[STACK_SIZE];
            init(reels[i]);
        }
        stackIndex = new int[size];
        stackCutter = new int[size];
    }
    public void shuffle(int cutter){
        for(int i=0;i<size;i++){
            shuffle(reels[i]);
        }
        reelIndex = 0;
        for(int i=0;i<size;i++){
            stackIndex[i] = 0;
            stackCutter[i] = STACK_SIZE-cutter-1;
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
    }

    public
    Tile swap(Tile tile){
        if(tile==null || !tile.swappable) throw new IllegalArgumentException("Must be a swappable tile");
        return fromCutter();
    }

    public Tile swap(Tile[] fourKind){
        if(fourKind==null || fourKind.length != 4) throw new IllegalArgumentException("Must be a four kind tiles");
        for(int i=1;i<4;i++){
            if(fourKind[i].rank != fourKind[i-1].rank){
                throw new IllegalArgumentException("Must be a four kind tiles");
            }
        }
        return fromCutter();
    }

    private synchronized Tile fromCutter(){
        Tile swap = tileList[reels[reelIndex][stackCutter[reelIndex]]];
        stackCutter[reelIndex]--;
        reelIndex = (reelIndex>=size-1)? 0 : (reelIndex+1);
        return swap;
    }

    public synchronized boolean draw(Tile[] slots){
        if(slots==null || slots.length==0) return false;
        boolean onHand = true;
        for(int i=0;i<slots.length;i++){
            if(stackIndex[reelIndex]==stackCutter[reelIndex]){
                onHand = false;
                break;
            }
            slots[i]=tileList[reels[reelIndex][stackIndex[reelIndex]]];
            stackIndex[reelIndex]++;
            reelIndex = (reelIndex>=size-1)? 0 : (reelIndex+1);
        }
        return onHand;
    }

    public synchronized int[] debug(){
        int[] debug = new int[size+1];
        debug[0]=reelIndex;
        for(int i=1;i<debug.length;i++){
            debug[i]=stackIndex[i-1];
        }
        return debug;
    }

    public static Stack stack(int reels){
        int size = reels;
        if(reels<MIN_REEL_SIZE) size = MIN_REEL_SIZE;
        if(reels>MAX_REEL_SIZE) size = MAX_REEL_SIZE;
        Stack stack = new Stack(size);
        return stack;
    }
}
