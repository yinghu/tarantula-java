package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LMDBDataStoreProvider implements DataStoreProvider {


    private Env<ByteBuffer> data;

    private String dir = "/var/tarantula/tds/lmdb";
    private long storeSize = 10_485_760;//10M
    private int maxDatabaseNumber = 1024;
    private int maxReaders = 16;

    private final static ConcurrentHashMap<String,LMDBDataStore> storeMap = new ConcurrentHashMap<>();
    @Override
    public void configure(Map<String, Object> properties) {
        dir = (String) properties.get("dir");
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
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = data.openDbi(name, DbiFlags.MDB_CREATE);
            Dbi<ByteBuffer> dbx = data.openDbi("ix_"+name, DbiFlags.MDB_DUPSORT);
            return new LMDBDataStore(name,dbi,dbx,data);
        });
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
        Path path = Paths.get(dir);
        if(!Files.exists(path)) Files.createDirectories(path);
        data = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path.toFile());
    }

    @Override
    public void shutdown() throws Exception {
        storeMap.forEach((k,v)->v.close());
        storeMap.clear();
        data.close();
    }
}
