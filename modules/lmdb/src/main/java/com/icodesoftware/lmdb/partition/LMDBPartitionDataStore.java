package com.icodesoftware.lmdb.partition;

import com.icodesoftware.Closable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

import com.icodesoftware.lmdb.LocalHeader;
import com.icodesoftware.service.DataStoreSummary;

import java.util.List;

public class LMDBPartitionDataStore implements DataStore,DataStore.Backup , Closable {

    private final LMDBPartitionProvider lmdbPartitionProvider;

    public LMDBPartitionDataStore(LMDBPartitionProvider lmdbPartitionProvider){
        this.lmdbPartitionProvider = lmdbPartitionProvider;
    }
    @Override
    public int scope() {
        return 0;
    }

    @Override
    public String name() {
        return "test_partition_user";
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        try(Recoverable.DataBufferPair cache = lmdbPartitionProvider.dataBufferPair()){
            Recoverable.DataBuffer  key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            lmdbPartitionProvider.assign(key);
            if(!t.readKey(key)){
                return false;
            }
            value.writeHeader(new LocalHeader(Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            if(!t.write(value)){
                return false;
            }
            return true;
        }
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

        return true;
    }

    @Override
    public <T extends Recoverable> boolean delete(T t) {

        return true;
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
        return List.of();
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
    public void forEachEdgeKeyValue(Recoverable.Key key, String label, BufferStream bufferStream) {

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

    @Override
    public void drop(boolean delete) {

    }
}
