package com.icodesoftware;

import java.util.List;

public interface Configuration extends Configurable {

    void property(String name,Object value);
    List<Property> properties();
    Object property(String name);
}
