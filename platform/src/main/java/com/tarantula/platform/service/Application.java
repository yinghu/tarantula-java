package com.tarantula.platform.service;

import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Event;
import com.icodesoftware.service.Serviceable;

/**
 * Developer: YINGHU LU
 * Date Updated: 8/7/2019
 */
public interface Application extends Serviceable {

    long DELTA = 60000;

    String LABEL = "LDA";

    Descriptor descriptor();

    Configuration configuration(String type);
    boolean launch(Instance instance);
    boolean checkAccessControl(Event event);

    default void atMidnight(){}

}
