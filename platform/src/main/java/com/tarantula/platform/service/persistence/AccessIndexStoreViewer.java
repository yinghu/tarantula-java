package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.KeyIndex;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.accessindex.DistributionAccessIndexViewer;


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


    public long totalRecords() {
        long rt = 0;
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = dataStore(i);
            rt += ds.count();
        }
        return rt;
    }

    public void list(DataStoreSummary.View view){
        boolean[] done = {false};
        for (int i=0;i< tarantulaContext.accessIndexRoutingNumber;i++){
            DataStore ds = dataStore(i);
            ds.backup().list((k,h,v)->{
                //if(view.on(tarantulaContext.node(),k,v)) return true;
                done[0] = true;
                return false;
            });
            if(done[0]) break;
        }
    }

    public void load(byte[] key, DataStoreSummary.View view){
        DataStore dataStore = dataStore(this.tarantulaContext.clusterProvider().partition(key));
        view.on(tarantulaContext.node(),key,dataStore.backup().get(key));
        KeyIndex keyIndex = tarantulaContext.keyIndexService.lookup(dataStore.name(),new String(key));
        if(keyIndex==null) return;
        ClusterProvider.Node[] nodes = tarantulaContext.keyIndexService.nodeList(keyIndex);
        DistributionAccessIndexViewer distributionDataViewer = (DistributionAccessIndexViewer) tarantulaContext.clusterProvider().accessIndexService();
        for(ClusterProvider.Node node : nodes){
            if(node==null) continue;
            byte[] ret = distributionDataViewer.load(dataStore.partitionNumber(),key,node);
            if(ret!=null) view.on(node,key,ret);
        }
    }
    private DataStore dataStore(int partition){
        return this.tarantulaContext.deploymentDataStoreProvider.lookup(AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX+partition);
    }
}
