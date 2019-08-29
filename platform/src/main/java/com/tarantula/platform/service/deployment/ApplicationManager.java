package com.tarantula.platform.service.deployment;

import com.tarantula.SchedulingTask;
import com.tarantula.TarantulaLogger;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.Instance;
import com.tarantula.platform.service.cluster.ApplicationBucketReceiver;

import java.util.concurrent.ScheduledFuture;

/**
 * Developer: YINGHU LU
 * Updated : 3/6/2019
 */
public class ApplicationManager extends DefaultApplication implements SchedulingTask {

    private InstanceManager[] managers;
    private ScheduledFuture appSchedule;
    private long duration;
    private boolean timed;
    private TarantulaLogger log = JDKLogger.getLogger(ApplicationManager.class);

    public ApplicationManager(TarantulaContext tarantulaContext, DeploymentDescriptor deploymentDescriptor){
        super(tarantulaContext,deploymentDescriptor);
    }

    @Override
    public void start() throws Exception{
        super.start();
        this.applicationRegistry = new ApplicationRegistry(this,this.tarantulaContext,this.deploymentDescriptor);
        this.applicationRegistry.configure();
        managers = new InstanceManager[this.tarantulaContext.platformRoutingNumber];
        for(int i=0;i<this.tarantulaContext.platformRoutingNumber;i++){
            //dispatch instances into different partitions via applicationId/partition
            InstanceManager pim = new InstanceManager(i,this);
            pim.start();
            managers[i]=pim;
            this.tarantulaContext.integrationCluster().registerBucketReceiver(new ApplicationBucketReceiver(pim.routingKey(),i,pim,pim));
        }
        this.duration = this.deploymentDescriptor.runtimeDuration();
        this.timed = this.duration>0;
        log.warn("Application ["+this.deploymentDescriptor.name()+"] is running on limited time ["+this.timed+"]");
        this.appSchedule = this.tarantulaContext.schedule(this);
    }
    public void configure(Instance instance){
        this.load(instance);
    }
    @Override
    public void shutdown() throws Exception{
        if(appSchedule!=null){
            appSchedule.cancel(true);
        }
        for(InstanceManager pim : managers){
            this.tarantulaContext.integrationCluster().unregisterBucketReceiver(pim.routingKey());
            pim.shutdown();
        }
        super.shutdown();
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return DELTA;
    }

    @Override
    public long delay() {
        return DELTA;
    }

    @Override
    public void run() {
        if(timed&&(duration-=DELTA)<=0){
            this.tarantulaContext.deploymentService().enableApplication(this.deploymentDescriptor.distributionKey(),false);
        }
        else{
            for(InstanceManager im: managers){
                im.onCheck();
            }
        }
    }
}
