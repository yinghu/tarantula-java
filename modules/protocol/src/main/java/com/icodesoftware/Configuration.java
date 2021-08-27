package com.icodesoftware;

import java.util.ArrayList;
import java.util.List;

public interface Configuration extends Configurable {

    void property(String name,Object value);
    default List<Property> properties(){return new ArrayList<>();}
    Object property(String name);
}
