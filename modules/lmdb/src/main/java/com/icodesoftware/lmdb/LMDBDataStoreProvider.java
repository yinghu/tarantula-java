package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.MapStoreListener;

import java.util.List;
import java.util.Map;

public class LMDBDataStoreProvider implements DataStoreProvider {
    @Override
    public void configure(Map<String, Object> properties) {

    }

    @Override
    public void registerMapStoreListener(int scope, MapStoreListener mapStoreListener) {

    }

    @Override
    public MapStoreListener mapStoreListener(int scope) {
        return null;
    }

    @Override
    public DataStore createAccessIndexDataStore(String name) {
        return null;
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return null;
    }

    @Override
    public DataStore create(String name, int partition) {
        return null;
    }

    @Override
    public List<String> list() {
        return null;
    }

    @Override
    public DataStore lookup(String name) {
        return null;
    }

    @Override
    public void backup(int scope) {

    }

    @Override
    public void backup(int scope, OnBackup backup) {

    }

    @Override
    public void recover(int scope, OnBackup backup) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
