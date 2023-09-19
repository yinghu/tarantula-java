package com.icodesoftware;

import java.util.List;

public interface DataStore {

    String bucket();

    String node();

    String name();

    long count();


    <T extends Recoverable> boolean create(T t);

    <T extends Recoverable> boolean update(T t);


    <T extends Recoverable> boolean createIfAbsent(T t, boolean loading);

    <T extends Recoverable> boolean load(T t);
    <T extends Recoverable> boolean delete(T t);

    <T extends Recoverable> boolean createEdge(T t,String label);
    boolean deleteEdge(Recoverable.Key key,Recoverable.Key edge,String label);
    boolean deleteEdge(Recoverable.Key key,String label);

    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> stream);

    Backup backup();

    interface Backup{

        boolean get(Recoverable.Key key, BufferStream buffer);
        boolean set(BufferStream bufferStream);
        void forEachEdgeKey(Recoverable.Key key,String label,BufferStream bufferStream);
        boolean setEdge(String label,BufferStream bufferStream);

        boolean unsetEdge(String label,BufferStream bufferStream,boolean fromLabel);
        boolean unset(BufferStream bufferStream);

        void forEach(BufferStream buffer);
    }


    interface Stream<T extends Recoverable>{
        boolean on(T t);
    }

    interface BufferStream{
       boolean on(Recoverable.DataBuffer keyBuffer,Recoverable.DataBuffer dataBuffer);
    }

    interface Updatable{
        void dataStore(DataStore dataStore);
        void update();
    }

}
