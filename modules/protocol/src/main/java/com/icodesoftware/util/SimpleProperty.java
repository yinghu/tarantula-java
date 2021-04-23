package com.icodesoftware.util;

import com.icodesoftware.Property;

public class SimpleProperty extends RecoverableObject implements Property {

    protected Object value;

    public SimpleProperty(String name,Object value){
        this.name = name;
        this.value = value;
    }

    @Override
    public Object value() {
        return this.value;
    }
}
