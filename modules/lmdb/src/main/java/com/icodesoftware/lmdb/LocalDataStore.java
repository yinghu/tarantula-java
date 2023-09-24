package com.icodesoftware.lmdb;

import com.icodesoftware.*;
import com.icodesoftware.service.DataStoreSummary;
import org.lmdbjava.Dbi;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.List;

public class LocalDataStore implements DataStore,DataStore.Backup , Closable {
    //private final LocalTransaction localTransaction;

    private final Txn<ByteBuffer> txn;
    //private final Dbi<ByteBuffer> dbi;
    private LMDBDataStore lmdbDataStore;
    public LocalDataStore(Txn<ByteBuffer> txn,LMDBDataStore lmdbDataStore){
        this.txn = txn;
        this.lmdbDataStore = lmdbDataStore;
    }
    @Override
    public void close() {
    }

    @Override
    public int scope() {
        return lmdbDataStore.scope();
    }

    @Override
    public String name() {
        return lmdbDataStore.name();
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        System.out.println("TXN : "+txn.getId());
        return lmdbDataStore.createEx(txn,t);
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean delete(T t) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean createEdge(T t, String label) {
        return false;
    }

    @Override
    public boolean deleteEdge(Recoverable.Key key, Recoverable.Key edge, String label) {
        return false;
    }

    @Override
    public boolean deleteEdge(Recoverable.Key key, String label) {
        return false;
    }

    @Override
    public <T extends Recoverable> List<T> list(RecoverableFactory<T> query) {
        return null;
    }

    @Override
    public <T extends Recoverable> void list(RecoverableFactory<T> query, Stream<T> stream) {

    }

    @Override
    public Backup backup() {
        return null;
    }

    @Override
    public boolean get(Recoverable.Key key, BufferStream buffer) {
        return false;
    }

    @Override
    public boolean set(BufferStream bufferStream) {
        return false;
    }

    @Override
    public void forEachEdgeKey(Recoverable.Key key, String label, BufferStream bufferStream) {

    }

    @Override
    public boolean setEdge(String label, BufferStream bufferStream) {
        return false;
    }

    @Override
    public boolean unsetEdge(String label, BufferStream bufferStream, boolean fromLabel) {
        return false;
    }

    @Override
    public boolean unset(BufferStream bufferStream) {
        return false;
    }

    @Override
    public void forEach(BufferStream buffer) {

    }

    @Override
    public void view(DataStoreSummary dataStoreSummary) {

    }
}
