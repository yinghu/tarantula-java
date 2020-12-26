package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.tarantula.platform.*;
import com.tarantula.platform.service.ApplicationAllocator;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Developer: YINGHU LU
 * Updated : 12/21/2020
 */
public class ApplicationRegistry implements ApplicationAllocator{

    private DeploymentDescriptor deploymentDescriptor;

    private TarantulaContext tarantulaContext;

    private final DefaultApplication eventDispatcher;
    private final DataStore dataStore;
    public ApplicationRegistry(final DefaultApplication defaultEventDispatcher,TarantulaContext tarantulaContext,DeploymentDescriptor deploymentDescriptor){
        this.eventDispatcher = defaultEventDispatcher;
        this.tarantulaContext = tarantulaContext;
        this.dataStore = tarantulaContext.masterDataStore();
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public void configure(){
        List<InstanceIndex> _rlist = tarantulaContext.queryFromDataMaster(PortableRegistry.OID,new InstanceRegistryQuery(deploymentDescriptor.distributionKey()),new String[]{deploymentDescriptor.distributionKey()},false);//this.tarantulaContext.tarantulaCluster.list(iq);
        if(_rlist.isEmpty()){
            _rlist = deployFromLocal(deploymentDescriptor);
        }
        for(InstanceIndex ir : _rlist){
            this.eventDispatcher.onAvailable.put(ir.distributionKey(),ir);
        }
    }
    public InstanceRegistry allocate(int partition) {
        InstanceIndex instanceRegistry = new InstanceIndex();
        instanceRegistry.capacity(deploymentDescriptor.capacity());
        instanceRegistry.applicationId(deploymentDescriptor.distributionKey());
        instanceRegistry.owner(deploymentDescriptor.distributionKey());
        instanceRegistry.accessMode(Session.FAST_PLAY_MODE);
        instanceRegistry.routingNumber(partition);
        instanceRegistry.bucket(dataStore.bucket());
        instanceRegistry.oid(SystemUtil.oid());
        if(this.tarantulaContext.masterDataStore().create(instanceRegistry)){
            this.eventDispatcher.onAvailable.put(instanceRegistry.distributionKey(),instanceRegistry);
            return instanceRegistry;
        }
        else{
            throw new RuntimeException("Cannot create instance registry");
        }
    }

    private List<InstanceIndex> deployFromLocal(DeploymentDescriptor deploymentDescriptor){
        ArrayList ilist = new ArrayList();
        for(int i=0;i<deploymentDescriptor.instancesOnStartupPerPartition();i++){
            for(int p = 0;p<tarantulaContext.platformRoutingNumber;p++){
                InstanceIndex instanceRegistry = new InstanceIndex();
                //instanceRegistry.bank(true);
                instanceRegistry.capacity(deploymentDescriptor.capacity());
                instanceRegistry.applicationId(deploymentDescriptor.distributionKey());
                instanceRegistry.owner(deploymentDescriptor.distributionKey());
                instanceRegistry.accessMode(Session.FAST_PLAY_MODE);
                instanceRegistry.routingNumber(p);
                instanceRegistry.bucket(dataStore.bucket());
                instanceRegistry.oid(SystemUtil.oid());
                if(dataStore.create(instanceRegistry)){
                    ilist.add(instanceRegistry);
                }
            }
        }
        return ilist;
    }
}
