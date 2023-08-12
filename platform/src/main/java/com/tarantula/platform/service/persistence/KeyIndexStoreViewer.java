package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.KeyIndexService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.keyindex.DistributionKeyIndexService;

public class KeyIndexStoreViewer implements KeyIndexService.KeyIndexStore {

    private TarantulaContext tarantulaContext;
    private DistributionKeyIndexService distributionKeyIndexService;

    public KeyIndexStoreViewer(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
        distributionKeyIndexService = tarantulaContext.clusterProvider().serviceProvider(DistributionKeyIndexService.NAME);
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
    public long totalRecords() {
        long rt = 0;
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = dataStore(i);
            rt += ds.count();
        }
        return rt;
    }

    public byte[] get(byte[] key){
        int partition = this.tarantulaContext.clusterProvider().partition(key);
        DataStore ds = dataStore(partition);
        return ds.backup().get(key);
    }
    public void list(DataStoreSummary.View view){
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
    public void load(byte[] key, DataStoreSummary.View view){
        DataStore dataStore = dataStore(this.tarantulaContext.clusterProvider().partition(key));
        view.on(tarantulaContext.node(),key,dataStore.backup().get(key));
        distributionKeyIndexService.load(dataStore.partitionNumber(),key,view);
    }

    private DataStore dataStore(int partition){
        return this.tarantulaContext.deploymentDataStoreProvider.lookup(KeyIndexService.KeyIndexStore.STORE_NAME_PREFIX+partition);
    }
}
