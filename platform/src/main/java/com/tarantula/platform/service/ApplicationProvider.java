package com.tarantula.platform.service;

import com.icodesoftware.Descriptor;
import com.icodesoftware.Event;
import com.icodesoftware.service.Serviceable;


public interface ApplicationProvider extends Serviceable {

    //long DELTA = 60000;

    String LABEL = "applications";

    Descriptor descriptor();

    boolean checkAccessControl(Event event);

    default void atMidnight(){}

}
