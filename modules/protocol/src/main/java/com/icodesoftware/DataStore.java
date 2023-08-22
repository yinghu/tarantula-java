package com.icodesoftware;


import java.util.List;

public interface DataStore {

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

    byte[] load(byte[] key);
    default boolean load(byte[] key, Buffer buffer){return false;}

    boolean delete(byte[] key);


    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> stream);

    Backup backup();

    interface Backup{
        boolean set(byte[] key,byte[] value);
        byte[] get(byte[] key);

        void unset(byte[] key);

        void list(Binary binary);
    }


    interface Stream<T extends Recoverable>{
        boolean on(T t);

    }
    interface Binary{
        boolean on(byte[] key,byte[] value);
    }
    interface Buffer{
       boolean on(Recoverable.DataBuffer dataBuffer);
    }

    interface Updatable{
        void dataStore(DataStore dataStore);
        void update();
    }

}
