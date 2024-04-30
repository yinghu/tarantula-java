package com.tarantula.game;


public class SimpleStub extends PlayerGameObject{

    public SimpleStub(String systemId,long stub){
        this.systemId = systemId;
        this.stub = stub;
    }
    public SimpleStub(){

    }
    @Override
    public String systemId(){
        return systemId;
    }
}
