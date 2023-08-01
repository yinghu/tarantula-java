package com.tarantula.platform.service.deployment;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.KeyIndexService;
import com.tarantula.platform.TarantulaContext;

public class KeyIndexStoreViewer implements KeyIndexService.KeyIndexStore {

    private TarantulaContext tarantulaContext;

    public KeyIndexStoreViewer(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;

    }

    @Override
    public String name() {
        return "KeyIndexStore";
    }

    @Override
    public int partitionNumber() {
        return tarantulaContext.accessIndexRoutingNumber;
    }

    @Override
    public long count() {
        long rt = 0;
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = dataStore(i);
            rt += ds.count();
        }
        return rt;
    }

    @Override
    public long count(int partition) {
        if(partition<0||partition>= tarantulaContext.accessIndexRoutingNumber) return 0;
        DataStore ds = dataStore(partition);
        return ds.count();
    }

    public boolean set(byte[] key,byte[] value){
        return false;
    }
    public byte[] get(byte[] key){
        int partition = this.tarantulaContext.clusterProvider().partition(key);
        DataStore ds = dataStore(partition);
        return ds.backup().get(key);
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

    @Override
    public void unset(byte[] key) {

    }

    private DataStore dataStore(int partition){
        return this.tarantulaContext.deploymentDataStoreProvider.lookup(KeyIndexService.KeyIndexStore.STORE_NAME_PREFIX+partition);
    }
}
