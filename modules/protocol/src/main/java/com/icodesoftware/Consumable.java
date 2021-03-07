package com.icodesoftware;

public interface Consumable extends Recoverable {

    String id();

    String name();
    void name(String name);

    String category();
    void category(String category);

    String description();
    void description(String description);

    void property(String pName,Object pValue);
    Object property(String pName);
}
