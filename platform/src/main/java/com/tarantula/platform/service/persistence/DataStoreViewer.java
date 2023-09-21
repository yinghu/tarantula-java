package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableRegistry;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.recover.DistributionDataViewer;

public class DataStoreViewer implements DataStoreSummary {

    private DataStore dataStore;
    private TarantulaContext tarantulaContext;
    public DataStoreViewer(TarantulaContext tarantulaContext,DataStore dataStore){
        this.tarantulaContext = tarantulaContext;
        this.dataStore = dataStore;
    }
    @Override
    public String name() {
        return dataStore.name();
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

    public void load(byte[] key, DataStoreSummary.View view){
        //view.on(tarantulaContext.node(),key,dataStore.backup().get(key));
        //KeyIndex keyIndex = tarantulaContext.keyIndexService.lookup(dataStore.name(),new BinaryKey(key));
        //if(keyIndex==null) return;
        //ClusterProvider.Node[] nodes = tarantulaContext.keyIndexService.nodeList(keyIndex);
        //DistributionDataViewer distributionDataViewer = (DistributionDataViewer) tarantulaContext.clusterProvider().recoverService();
        //for(ClusterProvider.Node node : nodes){
            //if(node==null) continue;
            //byte[] ret = distributionDataViewer.load(dataStore.name(),key,node);
            //if(ret!=null) view.on(node,key,ret);
        //}
    }
}
