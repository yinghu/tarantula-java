package com.tarantula.platform.service.deployment;

import com.icodesoftware.DataStore;
import com.icodesoftware.InstanceRegistry;
import com.icodesoftware.Session;
import com.tarantula.platform.*;
import com.tarantula.platform.service.ApplicationAllocator;
import com.tarantula.platform.util.SystemUtil;
import java.util.List;

/**
 * Developer: YINGHU LU
 * Updated : 02/15/2019
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
        List<InstanceIndex> _rlist = this.tarantulaContext.query(new String[]{deploymentDescriptor.distributionKey()},new InstanceRegistryQuery(deploymentDescriptor.distributionKey()));//this.tarantulaContext.tarantulaCluster.list(iq);
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
}
