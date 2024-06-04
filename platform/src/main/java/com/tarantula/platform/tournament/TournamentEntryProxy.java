package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

public class TournamentEntryProxy extends RecoverableObject implements Tournament.Entry  {

    private double delta;
    private double credit;
    @Override
    public long systemId() {
        return 0;
    }

    @Override
    public double score(double credit, double delta) {
        this.credit = credit;
        this.delta = delta;
        return delta;
    }

    @Override
    public double score() {
        return delta;
    }

    @Override
    public double credit() {
        return credit;
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

    public void rank(int rank){

    }
}
