package com.icodesoftware.lmdb.partition;

import com.icodesoftware.*;

import com.icodesoftware.lmdb.LocalDataStore;
import com.icodesoftware.lmdb.LocalEdgeDataStore;
import com.icodesoftware.lmdb.LocalHeader;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreSummary;

import java.util.List;

public class LMDBPartitionDataStore implements DataStore,DataStore.Backup , Closable {

    private static final TarantulaLogger logger = JDKLogger.getLogger(LMDBPartitionDataStore.class);
    private static final long REVISION_START_NUMBER = 1L;
    private final LMDBPartitionProvider lmdbPartitionProvider;
    private final int scope;
    private final String name;

    public LMDBPartitionDataStore(int scope,String name,LMDBPartitionProvider lmdbPartitionProvider){
        this.scope = scope;
        this.name = name;
        this.lmdbPartitionProvider = lmdbPartitionProvider;
    }
    @Override
    public int scope() {
        return scope;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        try(final Recoverable.DataBufferPair cache = lmdbPartitionProvider.dataBufferPair()){
            Recoverable.DataBuffer  key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            lmdbPartitionProvider.assign(key);
            key.flip();
            if(!t.readKey(key)){
                return false;
            }
            value.writeHeader(new LocalHeader(REVISION_START_NUMBER,t.getFactoryId(),t.getClassId()));
            if(!t.write(value)){
                return false;
            }
            key.rewind();
            LocalDataStore dataStore = lmdbPartitionProvider.partition(scope,name,key);
            key.rewind();
            value.flip();
            if(!dataStore.put(key,value)) return false;
            t.revision(REVISION_START_NUMBER);
            return true;
        } catch (Exception ex){
            logger.error("Error on create : ",ex);
            throw ex;
        }
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        try(final Recoverable.DataBufferPair cache = lmdbPartitionProvider.dataBufferPair()){
            Recoverable.DataBuffer  key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!t.writeKey(key)){
                return false;
            }
            key.flip();
            LocalDataStore dataStore = lmdbPartitionProvider.partition(scope,name,key);
            key.rewind();
            if(!dataStore.get(key,value)){
                return false;
            }
            value.flip();
            Recoverable.DataHeader header = value.readHeader();
            if(t.revision() != header.revision()) {
                return false;
            }
            value.clear();
            value.writeHeader(new LocalHeader(header.revision()+1,t.getFactoryId(),t.getClassId()));
            if(!t.write(value)){
                return false;
            }
            key.rewind();
            value.flip();
            if(!dataStore.put(key,value)) return false;
            t.revision(header.revision()+1);
            return true;
        } catch (Exception ex){
            logger.error("Error on update : ",ex);
            throw ex;
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        try(final Recoverable.DataBufferPair cache = lmdbPartitionProvider.dataBufferPair()){
            Recoverable.DataBuffer  key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!t.writeKey(key)){
                throw new RuntimeException("Key must be assigned first");
            }
            key.flip();
            LocalDataStore dataStore = lmdbPartitionProvider.partition(scope,name,key);
            key.rewind();
            if(dataStore.get(key,value)){
                if(!loading) return false;
                value.flip();
                Recoverable.DataHeader header = value.readHeader();
                t.read(value);
                t.revision(header.revision());
                return false;
            }
            value.clear();
            value.writeHeader(new LocalHeader(REVISION_START_NUMBER,t.getFactoryId(),t.getClassId()));
            if(!t.write(value)){
                throw new RuntimeException("Error on write value");
            }
            key.rewind();
            value.flip();
            t.revision(REVISION_START_NUMBER);
            return dataStore.put(key,value);
        } catch (Exception ex){
            logger.error("Error on createIfAbsent : ",ex);
            throw ex;
        }
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        try(Recoverable.DataBufferPair cache = lmdbPartitionProvider.dataBufferPair()){
            Recoverable.DataBuffer  key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!t.writeKey(key)){
                return false;
            }
            key.flip();
            LocalDataStore dataStore = lmdbPartitionProvider.partition(scope,name,key);
            key.rewind();
            if(!dataStore.get(key,value)) return false;
            value.flip();
            Recoverable.DataHeader header = value.readHeader();
            t.read(value);
            t.revision(header.revision());
            return true;
        }catch (Exception ex){
            logger.error("Error on load : ",ex);
            throw ex;
        }
    }

    @Override
    public <T extends Recoverable> boolean delete(T t) {
        try(Recoverable.DataBufferPair cache = lmdbPartitionProvider.dataBufferPair()){
            Recoverable.DataBuffer  key = cache.key();
            if(!t.writeKey(key)){
                return false;
            }
            key.flip();
            LocalDataStore dataStore = lmdbPartitionProvider.partition(scope,name,key);
            key.rewind();
            if(!dataStore.delete(key)) return false;
            return true;
        }catch (Exception ex){
            logger.error("Error on delete : ",ex);
            throw ex;
        }
    }

    @Override
    public <T extends Recoverable> boolean createEdge(T t, String label) {
        //LocalEdgeDataStore dataStore = lmdbPartitionProvider.localEdgeDataStore(scope,name,label);
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
