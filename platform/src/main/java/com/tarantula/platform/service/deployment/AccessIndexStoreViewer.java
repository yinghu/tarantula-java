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

    //@Override
    public long totalRecords() {
        long rt = 0;
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = dataStore(i);
            rt += ds.count();
        }
        return rt;
    }

    public void list(DataStore.Binary binary){
        boolean[] done = {false};
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = dataStore(i);
            ds.backup().list((k,v)->{
                if(binary.on(k,v)) return true;
                done[0] = true;
                return false;
            });
            if(done[0]) break;
        }
    }

    public void load(byte[] key, DataStore.Binary binary){
        int partition = this.tarantulaContext.clusterProvider().partition(key);
        binary.on(key,dataStore(partition).backup().get(key));
    }
    private DataStore dataStore(int partition){
        return this.tarantulaContext.deploymentDataStoreProvider.lookup(AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX+partition);
    }
}
