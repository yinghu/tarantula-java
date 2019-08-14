package com.tarantula.platform;

import com.tarantula.Distributable;

/**
 * Created by yinghu lu on 4/17/2018.
 */
abstract  public class DeploymentObject extends RecoverableObject {


    public int scope(){
        return Distributable.DATA_SCOPE;
    }

}
