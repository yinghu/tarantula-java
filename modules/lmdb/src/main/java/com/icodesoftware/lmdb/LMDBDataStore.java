package com.icodesoftware.lmdb;

import com.icodesoftware.Closable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BufferUtil;
import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LMDBDataStore implements DataStore,DataStore.Backup ,Closable {

    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> dbi;
    private final Dbi<ByteBuffer> index;
    private final String name;

    private final String bucket ="DBS";

    private Metadata metadata;

    //NOTES : key+value < 2040 bytes ( 511 bytes for key ; value <= 1521 bytes (2040 - 511 - 8)

    private final MapStoreListener mapStoreListener;
    public LMDBDataStore(String name, Dbi<ByteBuffer> dbi, Dbi<ByteBuffer> index,Env<ByteBuffer> env,MapStoreListener mapStoreListener){
        this.name = name;
        this.dbi = dbi;
        this.index = index;
        this.env = env;
        this.mapStoreListener = mapStoreListener;
    }

    @Override
    public String bucket() {
        return bucket;
    }

    @Override
    public String node() {
        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public int partitionNumber() {
        return 0;
    }

    @Override
    public long count(int partition) {
        return 0;
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        String akey = t.key().asString();
        if (akey != null) return false;
        t.bucket(this.bucket);
        t.oid(mapStoreListener.oid());
        akey = t.key().asString();
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.put(akey.getBytes(UTF_8)).flip();
        ByteBuffer value = ByteBuffer.allocateDirect(700);
        BufferProxy proxy = new BufferProxy(value);
        //proxy.writeBoolean(true);
        //proxy.writeLong(Long.MIN_VALUE);
        t.write(proxy);
        value.flip();
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        try{
            if(!dbi.put(txn,key,value)) return false;
            onEdge(t,key,txn);
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        String akey = t.key().asString();
        if(akey==null) return false;
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.put(akey.getBytes(UTF_8)).flip();
        ByteBuffer value = ByteBuffer.allocateDirect(700);
        t.write(new BufferProxy(value));
        value.flip();
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        try{
            if (dbi.get(txn, key) == null) return false;
            if(!dbi.put(txn,key,value)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        String akey = t.key().asString();
        if(akey==null) throw new IllegalArgumentException("Key must be assigned first");
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.put(akey.getBytes(UTF_8)).flip();
        Txn<ByteBuffer> txn = env.txnWrite(); //can be reading also
        try{
            if (dbi.get(txn, key) != null) {
                if (!loading) return false;
                t.read(new BufferProxy(txn.val()));
                return false;
            }
            ByteBuffer value = ByteBuffer.allocateDirect(700);
            t.write(new BufferProxy(value));
            value.flip();
            if (!dbi.put(txn, key, value)) throw new RuntimeException("lmdb failure to insert key/value");
            onEdge(t,key,txn);
            txn.commit();
            return true;
        }
        finally {
            txn.close();//rollback if exception
        }
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        String akey = t.key().asString();
        if(akey==null) return false;
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.put(akey.getBytes(UTF_8)).flip();
        Txn<ByteBuffer> txn = env.txnRead(); //read only
        try{
            if (dbi.get(txn, key) == null) return false;
            t.read(new BufferProxy(txn.val()));
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public byte[] load(byte[] key) {
        return new byte[0];
    }

    @Override
    public boolean delete(byte[] key) {
        Txn<ByteBuffer> txn = env.txnWrite(); //write/read txn
        ByteBuffer akey = ByteBuffer.allocateDirect(env.getMaxKeySize());
        akey.put(key).flip();
        try{
            if(!dbi.delete(txn, akey)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> List<T> list(RecoverableFactory<T> query) {
        ArrayList<T> list = new ArrayList<>();
        list(query,(t)-> list.add(t));
        return list;
    }

    @Override
    public <T extends Recoverable> void list(RecoverableFactory<T> query, Stream<T> stream){
        String akey = (query.distributionKey() + Recoverable.PATH_SEPARATOR + query.label());
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.put(akey.getBytes()).flip();
        Txn<ByteBuffer> txn = env.txnRead();
        CursorIterable<ByteBuffer> cursor = index.iterate(txn, KeyRange.closed(key, key));
        try{
            for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                T t = query.create();
                if(dbi.get(txn,kv.val())!=null){
                    t.read(new BufferProxy(txn.val()));
                    t.distributionKey(UTF_8.decode(kv.val()).toString());
                    if(!stream.on(t)) break;
                }
            }
        }finally {
            cursor.close();
            txn.close();
        }
    }
    @Override
    public Backup backup() {
        return this;
    }

    @Override
    public void close() {
        dbi.close();
        index.close();
    }

    @Override
    public boolean set(byte[] key, byte[] value) {
        return false;
    }

    @Override
    public byte[] get(byte[] key) {
        return new byte[0];
    }

    @Override
    public void unset(byte[] key) {

    }

    @Override
    public void list(Binary binary) {
        final Txn<ByteBuffer> txn = env.txnRead();
        final Cursor<ByteBuffer> cursor = dbi.openCursor(txn);
        while (cursor.next()){
            if(!binary.on(BufferUtil.toArray(cursor.key()),BufferUtil.toArray(cursor.val()))) break;
        }
        cursor.close();
        txn.close();
    }

    private <T extends Recoverable> void onEdge(T t,ByteBuffer edge,Txn<ByteBuffer> txn){
        if(!t.onEdge() || t.owner()==null || t.label()==null) return;
        String akey = t.owner()+Recoverable.PATH_SEPARATOR+t.label();
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.put(akey.getBytes()).flip();
        edge.rewind();
        index.put(txn,key,edge, PutFlags.MDB_NODUPDATA);
    }

}
