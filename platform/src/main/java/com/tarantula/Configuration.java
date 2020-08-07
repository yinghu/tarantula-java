package com.tarantula;

import java.util.List;

/**
 * Updated by yinghu on 7/19/2020
 */
public interface Configuration extends Configurable {

    String LABEL = "AFC";

    //String tag();
    //void tag(String tag);
    String type();
    void type(String type);

    void configure(String name,String value);
    List<Property> properties();
    String property(String name);
}
