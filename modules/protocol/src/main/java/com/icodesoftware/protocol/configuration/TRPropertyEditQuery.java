package com.icodesoftware.protocol.configuration;


import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class TRPropertyEditQuery implements RecoverableFactory<TRPropertyEdit> {


    private Recoverable.Key key;

    public TRPropertyEditQuery(Recoverable.Key key) {
        this.key = key;
    }
    @Override
    public TRPropertyEdit create() {
        return new TRPropertyEdit();
    }

    @Override
    public String label() {
        return TRPropertyEdit.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }

}
