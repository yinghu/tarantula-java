package com.tarantula.platform.service;

import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Event;
import com.icodesoftware.service.Serviceable;


public interface Application extends Serviceable {

    long DELTA = 60000;

    String LABEL = "LDA";

    Descriptor descriptor();

    Configuration configuration(String type);
    boolean checkAccessControl(Event event);

    default void atMidnight(){}

}
