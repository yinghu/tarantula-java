package com.tarantula.platform.service.persistence;

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

    public void list(DataStore.View view){
        boolean[] done = {false};
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = dataStore(i);
            ds.backup().list((k,v)->{
                if(view.on(tarantulaContext.node(),k,v)) return true;
                done[0] = true;
                return false;
            });
            if(done[0]) break;
        }
    }

    public void load(byte[] key, DataStore.View view){
        int partition = this.tarantulaContext.clusterProvider().partition(key);
        view.on(tarantulaContext.node(),key,dataStore(partition).backup().get(key));
    }
    private DataStore dataStore(int partition){
        return this.tarantulaContext.deploymentDataStoreProvider.lookup(AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX+partition);
    }
}
