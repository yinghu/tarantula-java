package com.icodesoftware;

import com.google.gson.JsonObject;


import java.util.ArrayList;
import java.util.List;

public interface Consumable extends Configuration {

    String id();

    String category();
    void category(String category);

    boolean isPack();
    void isPack(boolean isPack);

    boolean published();
    void published(boolean published);

    default JsonObject toJson(){ return null; }

    default <T extends Consumable> List<T> list(){ return new ArrayList<>();}


    interface Listener extends Configurable.Listener{
        void onCreated(Consumable consumable);
        void onUpdated(Consumable consumable);
    }
}
