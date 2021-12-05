package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

import java.util.List;

public class EmptyDataStore implements DataStore {
    @Override
    public int scope() {
        return 0;
    }

    @Override
    public String bucket() {
        return null;
    }

    @Override
    public String node() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }
    public int partitionNumber(){
        return 0;
    }

    public long count(int partition){
        return 0;
    }
    @Override
    public <T extends Recoverable> boolean create(T t) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean b) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        return false;
    }

    @Override
    public <T extends Recoverable> List<T> list(RecoverableFactory<T> recoverableFactory) {
        return null;
    }

    @Override
    public <T extends Recoverable> void list(RecoverableFactory<T> recoverableFactory, Stream<T> stream) {

    }

    @Override
    public void registerListener(int i, Listener listener) {

    }

    @Override
    public Backup backup() {
        return null;
    }
}
