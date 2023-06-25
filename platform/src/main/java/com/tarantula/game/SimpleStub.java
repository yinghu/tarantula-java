package com.tarantula.game;


public class SimpleStub extends PlayerGameObject{

    public SimpleStub(String systemId,int stub){
        this.systemId = systemId;
        this.stub = stub;
    }
    @Override
    public String systemId(){
        return systemId;
    }
}
