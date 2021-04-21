package com.icodesoftware;

import java.util.List;

public interface Configuration extends Configurable {

    String LABEL = "AFC";
    
    String type();
    void type(String type);

    void property(String name,Object value);
    List<Property> properties();
    Object property(String name);
}
