package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

public class TournamentEntryProxy extends RecoverableObject implements Tournament.Entry  {

    private double delta;
    @Override
    public String systemId() {
        return null;
    }

    @Override
    public void score(double credit, double delta) {
        this.delta = delta;
        System.out.println("score :"+delta);
    }

    @Override
    public double score() {
        return delta;
    }

    @Override
    public double credit() {
        return 0;
    }

    @Override
    public void finish() {

    }

    @Override
    public boolean finished() {
        return false;
    }

    @Override
    public int rank() {
        return 0;
    }
}
