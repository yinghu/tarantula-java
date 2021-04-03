package com.icodesoftware;

import java.util.List;

public interface Configuration extends Configurable {

    String LABEL = "AFC";
    
    String type();
    void type(String type);

    void configure(String name,String value);
    List<Property> properties();
    String property(String name);
}
