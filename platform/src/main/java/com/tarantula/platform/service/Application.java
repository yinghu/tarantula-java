package com.tarantula.platform.service;

import com.icodesoftware.service.Serviceable;
import com.tarantula.*;

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
