package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.Distributable;

/**
 * Created by yinghu lu on 4/17/2018.
 */
abstract  public class DeploymentObject extends RecoverableObject implements Portable {


    public int scope(){
        return Distributable.DATA_SCOPE;
    }

}
