package com.tarantula.platform;

import com.tarantula.Distributable;

/**
 * Updated by yinghu lu on 6/16/2020.
 */
public class NoReplicationObject extends RecoverableObject {


    public int scope(){
        return Distributable.LOCAL_SCOPE;
    }

}
