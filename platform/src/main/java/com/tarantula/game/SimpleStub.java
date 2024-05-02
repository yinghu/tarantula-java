package com.tarantula.game;


public class SimpleStub extends PlayerGameObject{

    public SimpleStub(long systemId,long stub){
        this.systemId = systemId;
        this.stub = stub;
    }
    public SimpleStub(){

    }
    @Override
    public long systemId(){
        return systemId;
    }
}
