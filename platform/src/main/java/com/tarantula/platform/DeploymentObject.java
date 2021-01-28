package com.tarantula.platform;

import com.icodesoftware.Distributable;
import com.icodesoftware.util.RecoverableObject;
/**
 * Updated by yinghu lu on 6/16/2020.
 */
abstract  public class DeploymentObject extends RecoverableObject {


    public int scope(){
        return Distributable.DATA_SCOPE;
    }

}
