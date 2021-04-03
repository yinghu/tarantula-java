package com.tarantula.platform;

import com.icodesoftware.Distributable;
import com.icodesoftware.util.RecoverableObject;

public class NoReplicationObject extends RecoverableObject {


    public int scope(){
        return Distributable.LOCAL_SCOPE;
    }
    public boolean distributable(){
        return false;
    }

}
