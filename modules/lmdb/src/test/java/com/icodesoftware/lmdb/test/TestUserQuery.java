package com.icodesoftware.lmdb.test;

import com.icodesoftware.RecoverableFactory;

public class TestUserQuery implements RecoverableFactory<TestUser> {

    public static final String OWNER_KEY_PREFIX = "testowner_";

    private String owner;
    public TestUserQuery(String owner){
        this.owner = owner;
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
        return OWNER_KEY_PREFIX+owner;
    }
}
