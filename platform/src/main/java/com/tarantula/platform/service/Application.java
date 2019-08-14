package com.tarantula.platform.service;

import com.tarantula.*;

/**
 * Developer: YINGHU LU
 * Date Updated: 8/7/2019
 */
public interface Application extends EventListener,Serviceable{

     long DELTA = 60000;

    void onCallback(Event event);

    Descriptor descriptor();

    Configuration configuration(String type);
    boolean launch(Instance instance);

}
