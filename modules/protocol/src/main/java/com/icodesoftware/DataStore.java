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
    <T extends Recoverable> boolean delete(T t);

    boolean load(Recoverable.Key key, BufferStream buffer);

    <T extends Recoverable> boolean createEdge(T t,String label);
    <T extends Recoverable> boolean deleteEdge(Recoverable.Key key,Recoverable.Key edge,String label);
    <T extends Recoverable> boolean deleteEdge(Recoverable.Key key,String label);

    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> stream);

    Backup backup();

    interface Backup{
        boolean set(BufferStream bufferStream);
        Recoverable.DataBuffer get(BufferStream bufferStream);
        boolean set(byte[] key,byte[] value);

        byte[] get(byte[] key);

        void unset(byte[] key);

        void list(BufferStream buffer);
    }


    interface Stream<T extends Recoverable>{
        boolean on(T t);

    }

    interface BufferStream{
       boolean on(Recoverable.DataBuffer keyBuffer, Recoverable.DataHeader dataHeader,Recoverable.DataBuffer dataBuffer);
    }

    interface Updatable{
        void dataStore(DataStore dataStore);
        void update();
    }

}
