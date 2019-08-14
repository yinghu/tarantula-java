package com.tarantula;

import java.util.List;

/**
 * Updated by yinghu on 9/21/2018.
 */
public interface Configuration extends Recoverable {

    String tag();
    void tag(String tag);
    String type();
    void type(String type);

    void configure(String name,String value);
    List<Property> properties();
    String property(String name);

    interface Listener{
        void onConfiguration(Configuration c);
    }
}
