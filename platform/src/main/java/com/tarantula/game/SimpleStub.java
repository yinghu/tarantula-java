package com.tarantula.game;


import com.icodesoftware.util.OnApplicationHeader;

public class SimpleStub extends OnApplicationHeader {

    public SimpleStub(long systemId,long stub){
        this.systemId = systemId;
        this.distributionId = systemId;
        this.stub = stub;
    }
}
