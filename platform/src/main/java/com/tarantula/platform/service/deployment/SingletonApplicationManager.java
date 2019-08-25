package com.tarantula.platform.service.deployment;

import com.tarantula.*;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.TarantulaApplicationContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.BucketReceiver;
import com.tarantula.platform.service.BucketReceiverListener;
import com.tarantula.platform.service.cluster.ApplicationBucketReceiver;
/**
 * Updated by yinghu 6/3/2019
 */
public class SingletonApplicationManager extends DefaultApplication implements BucketReceiverListener{

    private TarantulaApplicationContext singleton;

    private int accessMode;

    public SingletonApplicationManager(TarantulaContext tarantulaContext, DeploymentDescriptor deploymentDescriptor){
        super(tarantulaContext,deploymentDescriptor);
    }
    @Override
    public void start() throws Exception {
        super.start();
        DeploymentDescriptor dd = this.deploymentDescriptor.deploy(deploymentDescriptor.instanceId());
        dd.owner(dd.distributionKey());
        accessMode = dd.accessMode();
        this.singleton = this.launch(dd,null,this.deploymentDescriptor.resetEnabled());
        this.singleton._setup();//inject the app context proxy to decouple the TarantulaApplicationContext
        if(accessMode!=Session.PRIVATE_ACCESS_MODE){
            for(int r=0;r<this.tarantulaContext.platformRoutingNumber;r++){
                StringBuffer bs = new StringBuffer(this.tarantulaContext.dataBucketGroup).append(Recoverable.PATH_SEPARATOR).append(singleton.descriptor().tag()).append(Recoverable.PATH_SEPARATOR).append(r);
                this.tarantulaContext.integrationCluster.registerBucketReceiver(new ApplicationBucketReceiver(bs.toString(),r,this,this));
            }
        }
        else{
            this.singleton.log("["+singleton.descriptor().tag()+"] has no public access",OnLog.WARN);
        }
    }
    @Override
    public boolean onEvent(Event event){
        if(!event.forwarding()){
            this.onCallback(event);
        }
        else{
            this._onEvent(event);
        }
        return false;
    }
    public boolean _onEvent(Event event){
        switch (accessMode){
            case Session.PUBLIC_ACCESS_MODE:
                this.singleton.onEvent(event);
                break;
            case Session.PROTECT_ACCESS_MODE:
                this.singleton.onEvent(event);
                break;
            case Session.FORWARD_ACCESS_MODE:
                //CHECKING FORWARD TICKET BEFORE CALL APPLICATION
                this.singleton.onEvent(event);
                break;
            case Session.PRIVATE_ACCESS_MODE:
                this.singleton.onEvent(event);
                break;
            default:
                //no access
                this.singleton.onError(event,new IllegalAccessException("IllegalAccess ["+accessMode+"] on ["+event.toString()+"]"));
                break;
        }
        return true;
    }
    @Override
    public void onCallback(Event event) {
        switch (accessMode){
            case Session.PUBLIC_ACCESS_MODE:
                this.singleton.onRequestCallback(event);
                break;
            case Session.PROTECT_ACCESS_MODE:
            case Session.FORWARD_ACCESS_MODE:
                if(this.singleton.validator().onSession(event.systemId(),event.stub(),event.trackId(),event.ticket())){
                    this.singleton.onRequestCallback(event);
                }
                else{
                    this.singleton.onError(event,new IllegalAccessException("Session expired on ["+event.action()+"]"));
                }
                break;
            default:
                //no access
                this.singleton.onError(event,new IllegalAccessException("IllegalAccess"));
                break;
        }
    }

    @Override
    public void onBucketReceiver(int state,BucketReceiver bucketReceiver) {
        //forward state to singleton application to update app state
        this.singleton.onBucketReceiver(bucketReceiver.partition(),state);
    }
    public void shutdown() throws Exception{
        for(int r=0;r<this.tarantulaContext.platformRoutingNumber;r++){
            StringBuffer bs = new StringBuffer(this.tarantulaContext.dataBucketGroup).append(Recoverable.PATH_SEPARATOR).append(singleton.descriptor().tag()).append(Recoverable.PATH_SEPARATOR).append(r);
            this.tarantulaContext.integrationCluster.unregisterBucketReceiver(bs.toString());
        }
        this.singleton.clear();
    }
}
