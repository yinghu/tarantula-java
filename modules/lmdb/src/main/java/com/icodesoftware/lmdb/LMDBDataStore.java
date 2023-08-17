package com.icodesoftware.lmdb;

import com.icodesoftware.Closable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LMDBDataStore implements DataStore, Closable {

    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> dbi;
    private final String name;

    //NOTES : key+value < 2040 bytes ( 511 bytes for key ; value <= 1521 bytes (2040 - 511 - 8)

    public LMDBDataStore(String name, Dbi<ByteBuffer> dbi, Env<ByteBuffer> env){
        this.name = name;
        this.dbi = dbi;
        this.env = env;
    }

    @Override
    public String bucket() {
        return null;
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

        return false;
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        return false;
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        String akey = t.key().asString();
        if(akey==null) throw new IllegalArgumentException("Key must be assigned first");
        ByteBuffer key = ByteBuffer.allocateDirect(100);
        key.put(akey.getBytes(UTF_8)).flip();
        Txn<ByteBuffer> txn = env.txnWrite(); //can be reading also
        try{
            if (dbi.get(txn, key) != null) {
                if (!loading) return false;
                t.fromBinary(txn.val().array());
                return false;
            }
            ByteBuffer value = ByteBuffer.allocateDirect(700);
            value.put(t.toBinary()).flip();
            if (!dbi.put(txn, key, value)) throw new RuntimeException("lmdb failure to insert key/value");
            txn.commit();
        }finally {
            txn.close();//rollback if exception
        }
        return true;
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        return false;
    }

    @Override
    public byte[] load(byte[] key) {
        return new byte[0];
    }

    @Override
    public boolean delete(byte[] key) {
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
    public void close() {
        dbi.close();
    }
}
