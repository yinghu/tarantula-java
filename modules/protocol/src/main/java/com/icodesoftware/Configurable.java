package com.icodesoftware;


import com.google.gson.JsonObject;
import com.icodesoftware.service.ServiceContext;

public interface Configurable extends Recoverable, DataStore.Updatable {

    default <T extends Configurable> void registerListener(Listener<T> listener){}
    default void update(ServiceContext serviceContext){}

    default String configurationType(){return null;}
    default void configurationType(String configurationType){}
    default String configurationName(){return null;}
    default void configurationName(String configurationName){}
    default String configurationCategory(){return null;}
    default void configurationCategory(String configurationCategory){}

    default boolean configureAndValidate(byte[] data){ return false;}

    default JsonObject toJson(){ return new JsonObject();}

    interface Listener<T extends Configurable>{
        default void onCreated(T created){}
        default void onLoaded(T loaded){}
        default void onUpdated(T updated){}
        default void onRemoved(T removed){}
    }
}