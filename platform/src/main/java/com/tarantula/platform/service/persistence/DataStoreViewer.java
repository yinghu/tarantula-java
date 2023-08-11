package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.recover.DistributionDataViewer;

public class DataStoreViewer implements DataStore.Summary {

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
    public int partitionNumber() {
        return dataStore.partitionNumber();
    }

    @Override
    public long totalRecords() {
        return dataStore.count();
    }


    public void list(DataStore.View view){
        dataStore.backup().list((k,v)->view.on(tarantulaContext.node(),k,v));
    }

    public void load(byte[] key, DataStore.View view){
        view.on(tarantulaContext.node(),key,dataStore.backup().get(key));
        KeyIndex keyIndex = tarantulaContext.keyIndexService.lookup(dataStore.name(),new String(key));
        if(keyIndex==null) return;
        ClusterProvider.Node[] nodes = tarantulaContext.keyIndexService.nodeList(keyIndex);
        DistributionDataViewer distributionDataViewer = (DistributionDataViewer) tarantulaContext.clusterProvider().recoverService();
        for(ClusterProvider.Node node : nodes){
            System.out.println(node.memberId()+"#"+node.nodeName());
            byte[] ret = distributionDataViewer.load(dataStore.name(),key,node);
            //System.out.println(node.memberId()+"#"+node.nodeName()+">>"+new String(ret));
            if(ret!=null) view.on(node,key,ret);
        }
    }
}
