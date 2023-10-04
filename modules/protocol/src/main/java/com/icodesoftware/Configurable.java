package com.icodesoftware;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.service.ServiceContext;

import java.util.Map;

public interface Configurable extends Recoverable, DataStore.Updatable {

    String COMPONENT_CONFIG_TYPE = "component";

    String ASSET_CONFIG_TYPE = "asset";
    String COMMODITY_CONFIG_TYPE = "commodity";
    String ITEM_CONFIG_TYPE = "item";
    String APPLICATION_CONFIG_TYPE = "application";

    default <T extends Configurable> void registerListener(Listener<T> listener){}

    default void registered(){};
    default void released(){}
    default void updated(ServiceContext serviceContext){}

    default String configurationTypeId(){return null;}
    default void configurationTypeId(String configurationTypeId){}
    default String configurationName(){return null;}
    default void configurationName(String configurationName){}

    default String configurationType(){return null;}
    default void configurationType(String configurationType){}
    default String configurationCategory(){return null;}
    default void configurationCategory(String configurationCategory){}
    default String configurationVersion(){return null;}
    default void configurationVersion(String configurationVersion){}

    default boolean configureAndValidate(byte[] data){ return false;}
    default boolean configureAndValidate(Map<String,Object> data){ return false;}
    default boolean configureAndValidate(JsonObject payload){ return false;}
    default boolean configureAndValidate(){ return false;}

    default <T extends Configurable> T setup(){ return null;}
    default JsonObject toJson(){ return new JsonObject();}

    default JsonObject header(){ return null;}
    default JsonObject application(){ return null;}
    default JsonArray reference(){ return null;}
    interface Listener<T extends Configurable>{
        default void onCreated(T created){}
        default void onLoaded(T loaded){}
        default void onUpdated(T updated){}
        default void onRemoved(T removed){}
    }
}