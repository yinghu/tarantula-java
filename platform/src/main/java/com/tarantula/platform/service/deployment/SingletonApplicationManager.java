package com.tarantula.platform.service.deployment;

import com.icodesoftware.Access;
import com.icodesoftware.Event;
import com.icodesoftware.EventListener;
import com.icodesoftware.Recoverable;
import com.tarantula.*;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.TarantulaApplicationContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.event.EventOnAction;
import com.tarantula.platform.service.BucketReceiver;
import com.tarantula.platform.service.BucketReceiverListener;
import com.tarantula.platform.service.cluster.ApplicationBucketReceiver;
/**
 * Updated by yinghu 6/3/2019
 */
public class SingletonApplicationManager extends DefaultApplication implements BucketReceiverListener, EventListener {

    private TarantulaApplicationContext singleton;

    public SingletonApplicationManager(TarantulaContext tarantulaContext, DeploymentDescriptor deploymentDescriptor){
        super(tarantulaContext,deploymentDescriptor);
    }
    @Override
    public void start() throws Exception {
        super.start();
        DeploymentDescriptor dd = this.deploymentDescriptor.deploy(deploymentDescriptor.instanceId());
        dd.owner(dd.distributionKey());
        this.singleton = this.launch(dd,null);
        this.singleton._setup();//inject the app context proxy to decouple the TarantulaApplicationContext
        if(dd.accessMode()!= Access.PRIVATE_ACCESS_MODE){
            for(int r=0;r<this.tarantulaContext.platformRoutingNumber;r++){
                StringBuffer bs = new StringBuffer(this.tarantulaContext.dataBucketGroup).append(Recoverable.PATH_SEPARATOR).append(singleton.descriptor().tag()).append(Recoverable.PATH_SEPARATOR).append(r);
                this.tarantulaContext.integrationCluster().registerBucketReceiver(new ApplicationBucketReceiver(bs.toString(),r,this,this));
            }
        }
        else{
            this.singleton.log("["+singleton.descriptor().tag()+"] has no public access",OnLog.WARN);
        }
    }
    @Override
    public boolean checkAccessControl(Event event){
        if(this.deploymentDescriptor.accessControl()>0){ //check if caller role has enough access control
            return super.checkAccessControl(event)&&checkRole(event);
        }
        return super.checkAccessControl(event);
    }
    private boolean checkRole(Event event){
        return this.tarantulaContext.tokenValidatorProvider().role(event.systemId()).accessControl()>=this.deploymentDescriptor.accessControl();
    }
    @Override
    public boolean onEvent(Event event){
        try{
            if(event instanceof EventOnAction){
                if(this.checkAccessControl(event)){
                    this.singleton.actOnSingleton(event);
                }
                else{
                    throw new IllegalAccessException("Illegal access ->"+deploymentDescriptor.tag());
                }
            }
            else{
                if(checkRole(event)){
                    this.singleton.onEvent(event);
                }
                else{
                    throw new IllegalAccessException("Illegal access ->"+deploymentDescriptor.tag());
                }
            }
        }catch (Exception ex){
            this.singleton.onError(event,ex);
        }
        return false;
    }
    @Override
    public void onBucketReceiver(int state,BucketReceiver bucketReceiver) {
        //forward state to singleton application to update app state
        this.singleton.onBucketReceiver(bucketReceiver.partition(),state);
    }
    public void shutdown() throws Exception{
        for(int r=0;r<this.tarantulaContext.platformRoutingNumber;r++){
            StringBuffer bs = new StringBuffer(this.tarantulaContext.dataBucketGroup).append(Recoverable.PATH_SEPARATOR).append(singleton.descriptor().tag()).append(Recoverable.PATH_SEPARATOR).append(r);
            this.tarantulaContext.integrationCluster().unregisterBucketReceiver(bs.toString());
        }
        this.singleton.clear();
    }
}
