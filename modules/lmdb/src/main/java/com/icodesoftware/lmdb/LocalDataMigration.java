package com.icodesoftware.lmdb;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.service.DataStoreProvider;

import java.util.ArrayList;

public class LocalDataMigration {
    private final ArrayList<String> providers  = new ArrayList<>();
    public LocalDataMigration(JsonObject config){
        if(config==null) return;
        JsonArray listeners = config.get("listeners").getAsJsonArray();
        listeners.forEach(listener->{
            JsonObject provider = listener.getAsJsonObject();
            if(provider.get("enabled").getAsBoolean()) providers.add(provider.get("name").getAsString());
        });
    }

    public boolean migrating(){
        return providers.size()>0;
    }

    public void migrate(DataStoreProvider dataStoreProvider){
        providers.forEach(provider->{
            runMigration(provider,dataStoreProvider);
        });
    }

    private void runMigration(String migrationListener,DataStoreProvider dataStoreProvider){
        try{
            DataStoreProvider.MigrationListener migration = (DataStoreProvider.MigrationListener)Class.forName(migrationListener).getConstructor().newInstance();
            migration.migrate(dataStoreProvider);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
