package com.tarantula.platform;

import com.icodesoftware.OnSession;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class OnSessionQuery implements RecoverableFactory<SessionIndex> {


    private Recoverable.Key key;

    public OnSessionQuery(Recoverable.Key key){
        this.key = key;
    }

    @Override
    public SessionIndex create() {
        return new SessionIndex();
    }

    @Override
    public String label() {
        return OnSession.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
