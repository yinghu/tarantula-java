package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;

public class TestUserQuery implements RecoverableFactory<TestUser> {


    private String  ownerId;
    private String label;
    public TestUserQuery(String ownerId){
        this.ownerId = ownerId;
    }
    public TestUserQuery(String ownerId,String label){
        this.ownerId = ownerId;
        this.label = label;
    }
    @Override
    public TestUser create() {
        return new TestUser();
    }

    @Override
    public int registryId() {
        return 10;
    }

    @Override
    public String label() {
        return label==null?TestUser.LABEL:label;
    }
    
    @Override
    public Recoverable.Key key() {
        return new OidKey(ownerId);
    }
}
