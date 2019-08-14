package com.tarantula.platform;

import com.tarantula.Distributable;

/**
 * Created by yinghu lu on 10/2/2018.
 */
abstract public class IntegrationScopeObject extends RecoverableObject {

    public int scope(){
        return Distributable.INTEGRATION_SCOPE;
    }
}
