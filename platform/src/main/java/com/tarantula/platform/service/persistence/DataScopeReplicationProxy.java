package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;


public class DataScopeReplicationProxy extends ScopedReplicationProxy {
    private TarantulaLogger logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);
    public DataScopeReplicationProxy(){
        super(Distributable.DATA_SCOPE);
    }



}
