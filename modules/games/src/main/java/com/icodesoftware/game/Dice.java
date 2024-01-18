package com.icodesoftware.game;

import com.icodesoftware.service.RNG;
import com.icodesoftware.util.JvmRNG;

public class Dice {

    private final RNG rnd;
    private static final int[] diceSet ={1,2,3,4,5,6};
    private final int size;
    public Dice(int size){
        rnd = new JvmRNG();
        this.size = size;
    }
    public int[] roll(){
        int[] pts = rnd.onNextList(6,size);
        for(int i=0;i<size;i++){
            pts[i]= diceSet[pts[i]];
        }
        return pts;
    }
}
