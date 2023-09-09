package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class TestUserQuery implements RecoverableFactory<TestUser> {


    private long  ownerId;
    private String label;
    public TestUserQuery(long ownerId){
        this.ownerId = ownerId;
    }
    public TestUserQuery(long ownerId,String label){
        this.ownerId = ownerId;
        this.label = label;
    }
    @Override
    public TestUser create() {
        return new TestUser();
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
