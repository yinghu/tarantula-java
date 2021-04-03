package com.tarantula.platform;

import com.icodesoftware.Distributable;
import com.icodesoftware.util.RecoverableObject;

abstract  public class DeploymentObject extends RecoverableObject {


    public int scope(){
        return Distributable.DATA_SCOPE;
    }

}
