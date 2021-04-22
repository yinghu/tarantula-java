package com.tarantula.platform.service.deployment;

import com.icodesoftware.Configurable;

public class TypedListener {
    public final String type;
    public final Configurable.Listener listener;

    public TypedListener(String type,Configurable.Listener listener){
        this.type = type;
        this.listener = listener;
    }
}
