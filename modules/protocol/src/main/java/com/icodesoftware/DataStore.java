package com.icodesoftware;


import java.util.List;

public interface DataStore {

    //int scope();

    String bucket();

    String node();

    String name();

    long count();

    int partitionNumber();

    long count(int partition);

    <T extends Recoverable> boolean create(T t);

    <T extends Recoverable> boolean update(T t);

    <T extends Recoverable> boolean createIfAbsent(T t, boolean loading);

    <T extends Recoverable> boolean load(T t);

    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> stream);

    void registerListener(int registerId,Listener listener);
    Backup backup();

    interface Backup{
        void set(byte[] key,byte[] value);
        byte[] get(byte[] key);
        void list(Binary binary);
        void list(RecoverableFactory<Recoverable> query,Binary binary);
    }

    interface Listener{
        <T extends Recoverable> void onCreated(T t,String akey,byte[] key,byte[] value);
        <T extends Recoverable> void onUpdated(T t,String akey,byte[] key,byte[] value);
    }

    interface Stream<T extends Recoverable>{
        boolean on(T t);
    }
    interface Binary{
        boolean on(byte[] key,byte[] value);
    }

    interface Updatable{
        void dataStore(DataStore dataStore);
        void update();
    }
}
