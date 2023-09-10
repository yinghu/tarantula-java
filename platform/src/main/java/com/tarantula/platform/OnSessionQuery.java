package com.tarantula.platform;

import com.icodesoftware.OnSession;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

import com.tarantula.platform.service.cluster.PortableRegistry;

public class OnSessionQuery<T extends Recoverable> implements RecoverableFactory<T> {


    private Recoverable.Key key;

    public OnSessionQuery(Recoverable.Key key){
        this.key = key;
    }

    @Override
    public T create() {
        return new PortableRegistry<T>().create(PortableRegistry.ON_SESSION_CID);
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
