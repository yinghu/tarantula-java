package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class PlayerGameObject extends RecoverableObject {

    protected int stub;

    public int stub(){
        return stub;
    }

    public void stub(int stub){
        this.stub = stub;
    }
    
    public String systemId(){
        return this.bucket+ Recoverable.PATH_SEPARATOR+oid;
    }

}
