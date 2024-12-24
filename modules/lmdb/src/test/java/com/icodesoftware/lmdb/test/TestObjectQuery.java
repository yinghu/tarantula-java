package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class TestObjectQuery implements RecoverableFactory<TestObject> {


    private long  ownerId;
    private String label;

    public TestObjectQuery(long ownerId, String label){
        this.ownerId = ownerId;
        this.label = label;
    }
    @Override
    public TestObject create() {
        return new TestObject();
    }

    @Override
    public String label() {
        return label;
    }
    
    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(ownerId);
    }
}
