package com.tarantula.platform.presence.pvp;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class DefenseTeamIndexQuery implements RecoverableFactory<DefenseTeamIndex> {

    private Recoverable.Key key;
    private String label;

    public DefenseTeamIndexQuery(Recoverable.Key owner,String label){
        this.key = owner;
        this.label = label;
    }
    @Override
    public DefenseTeamIndex create() {
        return new DefenseTeamIndex();
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
