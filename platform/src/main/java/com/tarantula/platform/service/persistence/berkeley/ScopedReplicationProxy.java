package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.*;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.service.persistence.MapStoreListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ScopedReplicationProxy implements MapStoreListener,ServiceProvider, ClusterProvider.NodeListener {

    protected ServiceContext serviceContext;

    protected KeyIndexService keyIndexService;

    protected ClusterProvider.Node localNode;

    private ClusterProvider.Node[] pendingNodes;
    private ConcurrentHashMap<String, ClusterProvider.Node> nodeMappings;

    private AtomicInteger limit;

    protected DataStoreProvider dataStoreProvider;
    public ScopedReplicationProxy(DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
        limit = new AtomicInteger(-1);
        pendingNodes = new ClusterProvider.Node[0];
        nodeMappings = new ConcurrentHashMap<>();
    }

    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }


    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        return null;
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.keyIndexService = serviceContext.keyIndexService();
        this.localNode = serviceContext.node();
        this.serviceContext.clusterProvider().registerNodeListener(this);
    }

    @Override
    public void nodeAdded(ClusterProvider.Node node) {
        limit.getAndAccumulate(0,(x,y)->{
            nodeMappings.put(node.nodeName(),node);//mapping index
            if(pendingNodes.length==0){
                pendingNodes = new ClusterNode[1];
                pendingNodes[0]=node;
                return 0;
            }
            //check if node exist
            boolean existed = false;
            for(int i=0;i<pendingNodes.length;i++){
                if(pendingNodes[i].nodeName().equals(node.nodeName())){
                    pendingNodes[i]=node;//replacement
                    existed = true;
                    break;
                }
            }
            if(existed) return x;
            ClusterProvider.Node[] _p = new ClusterProvider.Node[pendingNodes.length+1];
            for(int i=0;i<pendingNodes.length;i++){
                _p[i+1] = pendingNodes[i];
            }
            _p[0]=node;
            pendingNodes = _p;
            return 0;
        });
    }

    @Override
    public void nodeRemoved(ClusterProvider.Node node) {
        limit.getAndAccumulate(0,(x,y)->{
            nodeMappings.remove(node.nodeName());//remove index
            if(pendingNodes.length==1){
                pendingNodes = new ClusterNode[0];
                return -1;
            }
            boolean existed = false;
            for(ClusterProvider.Node n : pendingNodes){
                if(n.nodeName().equals(node.nodeName())){
                    existed = true;
                    break;
                }
            }
            if(!existed) return x;
            ClusterProvider.Node[] _p = new ClusterProvider.Node[pendingNodes.length-1];
            int i = 0;
            for(ClusterProvider.Node n : pendingNodes){
                if(n.nodeName().equals(node.nodeName())) continue;
                _p[i++] = n;
            }
            pendingNodes = _p;
            return 0;
        });
    }

    public ClusterProvider.Node nextNode(){
        ClusterProvider.Node[] next = {null};
        limit.getAndAccumulate(1,(x,y)->{
            if(x==-1) return -1;
            next[0] = pendingNodes[x];
            return (x+y)<pendingNodes.length? (x+y):0;
        });
        return next[0];
    }
    public ClusterProvider.Node[] nextNodeList(int expected){
        ClusterProvider.Node[] next = new ClusterProvider.Node[expected];
        limit.getAndAccumulate(expected,(x,y)->{
            if(x==-1) return -1;
            int start = x;
            for(int i=0;i<expected;i++){
                if(i<pendingNodes.length){
                    if(start < pendingNodes.length){
                        next[i]=pendingNodes[start];
                    }
                    start = (start+1)<pendingNodes.length?(start+1):0;
                    if(start==x) break;
                }
            }
            return (x+y)<pendingNodes.length? (x+y):0;
        });
        return next;
    }

    public ClusterProvider.Node[] nodeList(KeyIndex keyIndex){
        boolean masterIncluded = !keyIndex.masterNode().equals(localNode.nodeName());
        String[] names = keyIndex.slaveNodes();
        ClusterProvider.Node[] slaves = new ClusterProvider.Node[masterIncluded?names.length+1:names.length];
        int start = 0;
        if(masterIncluded) {
            slaves[0] = nodeMappings.get(keyIndex.masterNode());
            start = 1;
        }
        for(int i=start; i<names.length;i++){
            slaves[i] = nodeMappings.get(names[i]);
        }
        return slaves;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
