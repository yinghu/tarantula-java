package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class TestAccessQuery implements RecoverableFactory<TestAccessIndex> {


    private long  ownerId;
    private String label;
    public TestAccessQuery(long ownerId){
        this.ownerId = ownerId;
    }
    public TestAccessQuery(long ownerId, String label){
        this.ownerId = ownerId;
        this.label = label;
    }
    @Override
    public TestAccessIndex create() {
        return new TestAccessIndex();
    }

    @Override
    public String label() {
        return label==null?TestUser.LABEL:label;
    }
    
    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(ownerId);
    }
}
