package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class OversizeDataIndexQuery implements RecoverableFactory<OversizeDataIndex> {

    private Recoverable.Key key;

    public OversizeDataIndexQuery(Recoverable.Key key){
        this.key = key;
    }

    @Override
    public OversizeDataIndex create() {
        return new OversizeDataIndex();
    }


    @Override
    public String label() {
        return OversizeDataIndex.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
