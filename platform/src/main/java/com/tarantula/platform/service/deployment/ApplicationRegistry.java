package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.service.RecoverService;
import com.tarantula.platform.*;
import com.tarantula.platform.service.ApplicationAllocator;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
        List<InstanceIndex> _rlist = query(PortableRegistry.OID,new InstanceRegistryQuery(deploymentDescriptor.distributionKey()),new String[]{deploymentDescriptor.distributionKey()});//this.tarantulaContext.tarantulaCluster.list(iq);
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
    private <T extends Recoverable> List<T> query(int factoryId, RecoverableFactory<T> factory, String[] params){
        RecoverService recoverService = tarantulaContext.tarantulaCluster().recoverService();
        List<T> tlist = new ArrayList<>();
        CountDownLatch _lock = new CountDownLatch(1);
        String cid = this.tarantulaContext.deploymentService().distributionCallback().registerQueryCallback((k,v)->{
            System.out.println(new String(v));
            T t = factory.create();
            t.fromBinary(v);
            t.distributionKey(new String(k));
            tlist.add(t);
        },()->{
            _lock.countDown();
            System.out.println("end query");
        });
        recoverService.queryStart(cid,tarantulaContext.dataStoreMaster,factoryId,factory.registryId(),params);
        try {
            _lock.await();
        }catch (Exception ex){}
        this.tarantulaContext.deploymentService().distributionCallback().removeQueryCallback(cid);
        return tlist;
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
