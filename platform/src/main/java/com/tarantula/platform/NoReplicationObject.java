package com.tarantula.platform;

import com.tarantula.Distributable;

/**
 * Created by yinghu lu on 4/17/2018.
 */
public class NoReplicationObject extends RecoverableObject {


    public int scope(){
        return Distributable.LOCAL_SCOPE;
    }

}
