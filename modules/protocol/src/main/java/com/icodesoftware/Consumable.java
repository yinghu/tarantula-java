package com.icodesoftware;

import java.util.List;

public interface Consumable extends Recoverable {

    String id();

    String name();
    void name(String name);

    String type();
    void type(String type);

    String category();
    void category(String category);

    String description();
    void description(String description);

    boolean isPack();
    void isPack(boolean isPack);

    boolean published();
    void published(boolean published);

    default <T extends Consumable> List<T> list(){ return null;}

    void property(String pName,Object pValue);
    Object property(String pName);

    interface Listener{
        void onCreated(Consumable consumable);
        void onUpdated(Consumable consumable);
    }
}
