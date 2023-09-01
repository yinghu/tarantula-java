package com.icodesoftware;


import java.nio.ByteBuffer;
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
    default <T extends Recoverable> boolean delete(T t){ return false;}
    default byte[] load(byte[] key){return null;}
    default boolean load(Recoverable.Key key, Buffer buffer){return false;}

    default <T extends Recoverable> boolean createEdge(T t,String label){return false;}
    default <T extends Recoverable> boolean deleteEdge(Recoverable.Key key,Recoverable.Key edge,String label){return false;}
    default <T extends Recoverable> boolean deleteEdge(Recoverable.Key key,String label){return false;}
    default boolean delete(byte[] key){return false;}

    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> stream);

    Backup backup();

    interface Backup{
        boolean set(byte[] key,byte[] value);

        default boolean set(ByteBuffer key, ByteBuffer value){return false;}
        byte[] get(byte[] key);

        void unset(byte[] key);

        default void list(Binary binary){}
        default void list(Buffer buffer){}
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
