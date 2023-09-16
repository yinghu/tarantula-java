package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableRegistry;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.KeyIndexService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.keyindex.DistributionKeyIndexService;

public class KeyIndexStoreViewer implements KeyIndexService.KeyIndexStore {

    private TarantulaContext tarantulaContext;
    private DistributionKeyIndexService distributionKeyIndexService;

    private DataStore dataStore;
    public KeyIndexStoreViewer(TarantulaContext tarantulaContext,DataStore dataStore){
        this.tarantulaContext = tarantulaContext;
        distributionKeyIndexService = tarantulaContext.clusterProvider().serviceProvider(DistributionKeyIndexService.NAME);
        this.dataStore = dataStore;
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
        return dataStore.count();
    }


    public void list(DataStoreSummary.View view){
        dataStore.backup().forEach((k,v)-> {
            Recoverable.DataHeader h = v.readHeader();
            RecoverableRegistry registry = tarantulaContext.recoverableRegistry(h.factoryId());
            Recoverable r = registry.create(h.classId());
            r.readKey(k);
            r.read(v);
            return view.on(tarantulaContext.node(),h,r);
        });
    }
    public byte[] get(byte[] key){
        int partition = this.tarantulaContext.clusterProvider().partition(key);
        DataStore ds = dataStore(partition);
        return null;//ds.backup().get(key);
    }

    public void load(byte[] key, DataStoreSummary.View view){
        DataStore dataStore = dataStore(this.tarantulaContext.clusterProvider().partition(key));
        //view.on(tarantulaContext.node(),key,dataStore.backup().get(key));
        distributionKeyIndexService.load(dataStore.partitionNumber(),key,view);
    }

    private DataStore dataStore(int partition){
        return this.tarantulaContext.deploymentDataStoreProvider.lookup(KeyIndexService.KeyIndexStore.STORE_NAME);
    }
}
