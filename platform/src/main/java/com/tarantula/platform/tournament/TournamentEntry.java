package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

public class TournamentEntry extends RecoverableObject implements Tournament.Entry {
    @Override
    public String systemId() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void name(String s) {

    }

    @Override
    public String icon() {
        return null;
    }

    @Override
    public void icon(String s) {

    }

    @Override
    public double score(double v) {
        return 0;
    }
}
