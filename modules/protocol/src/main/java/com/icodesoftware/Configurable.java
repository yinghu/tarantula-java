package com.icodesoftware;


import com.icodesoftware.service.ServiceContext;

public interface Configurable extends Recoverable, DataStore.Updatable {

    default <T extends Configurable> void registerListener(Listener listener){}
    default void update(ServiceContext serviceContext){}

    default String configurationType(){return null;}
    default void configurationType(String configurationType){}
    default String configurationName(){return null;}
    default void configurationName(String configurationName){}
    default String configurationCategory(){return null;}
    default void configurationCategory(String configurationCategory){}

    default boolean configureAndValidate(byte[] data){ return false;}

    interface Listener{
        default <T extends Configurable> void onCreated(T created){}
        default <T extends Configurable> void onUpdated(T updated){}
    }
}