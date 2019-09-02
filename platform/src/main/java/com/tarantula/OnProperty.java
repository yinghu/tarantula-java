package com.tarantula;

import java.util.List;

/**
 * Updated by yinghu lu on 9/2/2019
 */
public interface OnProperty {

    void property(String header,String value);
    String property(String header);

    List<Property> list();
}
