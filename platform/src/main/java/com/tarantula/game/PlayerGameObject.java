package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class PlayerGameObject extends RecoverableObject {

    public String systemId(){
        return this.bucket+ Recoverable.PATH_SEPARATOR+oid;
    }
}
