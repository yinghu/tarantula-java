package com.tarantula.game.casino;

import com.tarantula.game.GameComponent;

public class CountOnRank extends GameComponent {
    public final int rank;
    public int count;
    public CountOnRank(int rank,String label){
        this.rank = rank;
        this.name = "countOnRank";
        this.label = label;
    }
}
