package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.LongTypeKey;

public class TestUserQuery implements RecoverableFactory<TestUser> {

    public static final String OWNER_KEY_PREFIX = "testowner_";

    private long  ownerId;
    public TestUserQuery(long ownerId){
        this.ownerId = ownerId;
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
        return TestUser.LABEL;
    }

    @Override
    public String distributionKey() {
        return OWNER_KEY_PREFIX;
    }

    @Override
    public Recoverable.Key key() {
        return new LongTypeKey(ownerId);
    }
}
