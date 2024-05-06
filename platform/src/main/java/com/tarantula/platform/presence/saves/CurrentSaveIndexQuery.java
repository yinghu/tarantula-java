package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Session;

public class CurrentSaveIndexQuery implements RecoverableFactory<CurrentSaveIndex> {

    private Session session;

    public CurrentSaveIndexQuery(Session session){
        this.session = session;
    }

    @Override
    public CurrentSaveIndex create() {
        return new CurrentSaveIndex();
    }


    @Override
    public String label() {
        return CurrentSaveIndex.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return session.key();
    }
}
