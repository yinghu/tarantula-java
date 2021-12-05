package com.tarantula.platform.service.deployment;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.AccessIndexService;
import com.tarantula.platform.TarantulaContext;

public class AccessIndexStoreViewer implements AccessIndexService.AccessIndexStore {

    private TarantulaContext tarantulaContext;

    public AccessIndexStoreViewer(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;

    }

    @Override
    public String name() {
        return "AccessIndexStore";
    }

    @Override
    public int partitionNumber() {
        return tarantulaContext.accessIndexRoutingNumber;
    }

    @Override
    public long count() {
        long rt = 0;
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = tarantulaContext.dataStore(AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX+i);
            rt += ds.count();
        }
        return rt;
    }

    @Override
    public long count(int partition) {
        if(partition<0||partition>= tarantulaContext.accessIndexRoutingNumber) return 0;
        DataStore ds = tarantulaContext.dataStore(AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX+partition);
        return ds.count();
    }
}
