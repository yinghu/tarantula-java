package com.icodesoftware;

import com.google.gson.JsonObject;
import com.icodesoftware.service.DeployableListener;

import java.util.ArrayList;
import java.util.List;

public interface Consumable extends Deployable {

    String id();

    String name();
    void name(String name);

    String type();
    void type(String type);

    String category();
    void category(String category);

    boolean isPack();
    void isPack(boolean isPack);

    boolean published();
    void published(boolean published);

    default JsonObject toJson(){ return null; }

    default <T extends Consumable> List<T> list(){ return new ArrayList<>();}

    void property(String pName,Object pValue);
    Object property(String pName);

    interface Listener extends DeployableListener {
        void onCreated(Consumable consumable);
        void onUpdated(Consumable consumable);
    }
}
