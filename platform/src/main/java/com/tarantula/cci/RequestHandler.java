package com.tarantula.cci;

import com.icodesoftware.EventListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;

public interface RequestHandler extends EventListener, Serviceable {
    String name();
    void onRequest(OnExchange exchange);
    void setup(ServiceContext tcx);
    default void onCheck(){}
    default boolean deployable(){return false;}
}
