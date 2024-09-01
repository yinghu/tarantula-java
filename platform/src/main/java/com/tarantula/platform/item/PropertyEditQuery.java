package com.tarantula.platform.item;


import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class PropertyEditQuery implements RecoverableFactory<PropertyEdit> {


    private Recoverable.Key key;

    public PropertyEditQuery(Recoverable.Key key) {
        this.key = key;
    }
    @Override
    public PropertyEdit create() {
        return new PropertyEdit();
    }

    @Override
    public String label() {
        return PropertyEdit.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }

}
