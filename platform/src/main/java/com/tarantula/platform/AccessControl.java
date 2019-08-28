package com.tarantula.platform;

import com.tarantula.Access;

public class AccessControl implements Access.Role {

    private final String name;
    private final int accessControl;

    public AccessControl(final String name,final int accessControl){
        this.name = name;
        this.accessControl = accessControl;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public int accessControl() {
        return accessControl;
    }
}
