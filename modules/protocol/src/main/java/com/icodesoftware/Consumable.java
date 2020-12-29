package com.icodesoftware;

public interface Consumable extends Recoverable{
    String id();
    String name();
    void name(String name);
    String description();
    void description(String description);
    void property(String pName,Object pValue);
    Object property(String pName);
}
