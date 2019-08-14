package com.tarantula.game.scc;

import com.tarantula.game.GameComponent;

public class DiceSide extends GameComponent {
    public int rank; //1 - 6
    public boolean released; //

    public DiceSide(){
        this.name = "dice";
        this.label = "scc";
    }

    public DiceSide(int rank,boolean released){
        this();
        this.rank = rank;
        this.released = released;
    }
    public String toString(){
        return "Dice rank ["+rank+"/"+released+"]";
    }
}
