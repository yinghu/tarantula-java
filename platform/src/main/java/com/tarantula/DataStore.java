package com.tarantula;


import java.util.List;

/**
 * Updated by yinghu on 6/16/2019.
 */
public interface DataStore{

    int scope();

    String bucket();

    String node();

    String name();

    long count();

    <T extends Recoverable> boolean create(T t);

    <T extends Recoverable> int create(T[] tb);

    <T extends Recoverable> boolean update(T t);

    <T extends Recoverable> boolean createIfAbsent(T t, boolean loading);
    <T extends Recoverable> boolean load(T t);


    void traverse(Overflow overflow);
    
    void set(byte[] key,byte[] value);
    byte[] get(byte[] key);
    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> stream);


    RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener);
    void unregisterRecoverableListener(int factoryId);

    interface Stream<T extends Recoverable>{
        boolean on(T t);
    }
    interface Overflow{
        boolean on(String ds,int partition,byte[] key,byte[] value);
    }
    interface Updatable{
        void dataStore(DataStore dataStore);
        void update();
    }
}
