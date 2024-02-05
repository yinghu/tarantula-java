package com.tarantula.platform.service.persistence;

import com.icodesoftware.*;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.accessindex.DistributionAccessIndexViewer;
import com.tarantula.platform.service.cluster.recover.DistributionDataViewer;

import java.util.Base64;
import java.util.List;

public class DataStoreViewer implements DataStoreSummary {

    private DataStore dataStore;
    private TarantulaContext tarantulaContext;

    private long count;
    private int depth;
    private int pageSize;

    private long leafPages;
    private long branchPages;
    private long overflowPages;

    private List<String> edgeList;

    public DataStoreViewer(TarantulaContext tarantulaContext,DataStore dataStore){
        this.tarantulaContext = tarantulaContext;
        this.dataStore = dataStore;
        this.dataStore.backup().view(this);
    }

    public DataStoreViewer(){
    }
    @Override
    public String name() {
        return dataStore.name();
    }

    public int scope(){
        return dataStore.scope();
    }

    public long count(){
        return count;
    }
    public int depth(){
        return depth;
    }
    public int pageSize(){
        return pageSize;
    }

    public long leafPages(){
        return leafPages;
    }

    public long overflowPages(){
        return overflowPages;
    }

    public long branchPages(){
        return branchPages;
    }

    public List<String> edgeList(){
        return edgeList;
    }

    public void count(long count){
        this.count = count;
    }
    public void depth(int depth){
        this.depth = depth;
    }

    public void pageSize(int pageSize){
        this.pageSize = pageSize;
    }

    public void leafPages(long leafPages){
        this.leafPages = leafPages;
    }

    public void overflowPages(long overflowPages){
        this.overflowPages = overflowPages;
    }

    public void branchPages(long branchPages){
        this.branchPages = branchPages;
    }

    public void edgeList(List<String> edgeList){
        this.edgeList = edgeList;
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
        BinaryKey akey = new BinaryKey(Base64.getDecoder().decode(key));
        if(dataStore.scope()== Distributable.INTEGRATION_SCOPE){
            DistributionAccessIndexViewer viewer = (DistributionAccessIndexViewer) tarantulaContext.clusterProvider().accessIndexService();
            viewer.scan(akey.asBinary(),(m,k,v)->{
                ClusterProvider.Node node = tarantulaContext.clusterProvider().summary().node(m.getUuid());
                tarantulaContext.log("aaa : "+node.nodeName(), OnLog.WARN);
                //view.on(null,v.readHeader(),)
                return true;
            });
            return;
        }
        if(dataStore.scope()== Distributable.DATA_SCOPE){
            DistributionDataViewer viewer = (DistributionDataViewer) tarantulaContext.clusterProvider().recoverService();
            viewer.scan(dataStore.name(),akey.asBinary(),(m,k,v)->{
                ClusterProvider.Node node = tarantulaContext.clusterProvider().summary().node(m.getUuid());
                tarantulaContext.log("bbb : "+node.nodeName(), OnLog.WARN);
                return true;
            });
        }
    }
}
