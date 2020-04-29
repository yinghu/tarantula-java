package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.cci.*;
import com.tarantula.platform.TarantulaContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Developer: YINGHU LU
 * Date updated 7/15/19
 */
public class EndpointService implements Serviceable,EndPoint.Resource,SchedulingTask{

    TarantulaContext tarantulaContext;
    private HashMap<String,RequestHandler> rMap = new HashMap<>();
    private final ArrayList<EndPoint> endPointList;
    private final PushEventHandler pushEventHandler;
    private ScheduledFuture scheduledFuture;
    public EndpointService(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
        this.endPointList = new ArrayList<>();
        this.pushEventHandler = new PushEventHandler();
    }

    public void start() throws Exception {
        AccessIndexService ais = (AccessIndexService)this.tarantulaContext.serviceProvider(AccessIndexService.NAME);
        DeploymentServiceProvider dis = this.tarantulaContext.deploymentService();
        RootContentHandler rootContentHandler = new RootContentHandler();
        rootContentHandler.setup(this.tarantulaContext.tokenValidatorProvider().tokenValidator(),this.tarantulaContext.eventService(Distributable.INTEGRATION_SCOPE),ais,this.tarantulaContext.dataBucketGroup,dis);
        rootContentHandler.start();
        rMap.put(rootContentHandler.name(),rootContentHandler);

        UploadEventHandler uploadEventHandler = new UploadEventHandler();
        uploadEventHandler.setup(this.tarantulaContext.tokenValidatorProvider().tokenValidator(),this.tarantulaContext.eventService(Distributable.INTEGRATION_SCOPE),ais,this.tarantulaContext.dataBucketGroup,dis);
        uploadEventHandler.start();
        rMap.put(uploadEventHandler.name(),uploadEventHandler);

        ResourceEventHandler resourceEventHandler = new ResourceEventHandler();
        resourceEventHandler.setup(this.tarantulaContext.tokenValidatorProvider().tokenValidator(),this.tarantulaContext.eventService(Distributable.INTEGRATION_SCOPE),ais,this.tarantulaContext.dataBucketGroup,dis);
        resourceEventHandler.start();
        rMap.put(resourceEventHandler.name(),resourceEventHandler);

        UserEventHandler userHandler = new UserEventHandler();
        userHandler.setup(this.tarantulaContext.tokenValidatorProvider().tokenValidator(),this.tarantulaContext.eventService(Distributable.INTEGRATION_SCOPE),ais,this.tarantulaContext.dataBucketGroup,dis);
        userHandler.start();
        rMap.put(userHandler.name(),userHandler);

        ServiceEventHandler serviceEventHandler = new ServiceEventHandler();
        serviceEventHandler.setup(this.tarantulaContext.tokenValidatorProvider().tokenValidator(),this.tarantulaContext.eventService(Distributable.INTEGRATION_SCOPE),ais,this.tarantulaContext.dataBucketGroup,dis);
        serviceEventHandler.start();
        rMap.put(serviceEventHandler.name(),serviceEventHandler);

        ApplicationEventHandler applicationHandler = new ApplicationEventHandler();
        applicationHandler.setup(this.tarantulaContext.tokenValidatorProvider().tokenValidator(),this.tarantulaContext.eventService(Distributable.INTEGRATION_SCOPE),ais,this.tarantulaContext.dataBucketGroup,dis);
        applicationHandler.start();
        rMap.put(applicationHandler.name(),applicationHandler);

        //PushEventHandler pushEventHandler = new PushEventHandler(this.endPointListenerList);
        pushEventHandler.setup(this.tarantulaContext.tokenValidatorProvider().tokenValidator(),this.tarantulaContext.eventService(Distributable.INTEGRATION_SCOPE),ais,this.tarantulaContext.dataBucketGroup,dis);
        pushEventHandler.start();
        rMap.put(pushEventHandler.name(),pushEventHandler);

        for(EndPoint endPoint : endPointList){
            endPoint.resource(this);
            endPoint.start();
            endPoint.setup(this.tarantulaContext);
            endPoint.waitForData();
            this.tarantulaContext.node_started.set(true);
        }
        this.scheduledFuture = this.tarantulaContext.schedule(this);
    }

    public void shutdown() throws Exception {
        if(scheduledFuture!=null){
            this.scheduledFuture.cancel(true);
        }
        for(EndPoint endpoint : endPointList){
            endpoint.shutdown();
        }
    }
    public void addEndPoint(EndPoint endPoint){
        this.endPointList.add(endPoint);
    }

    @Override
    public RequestHandler requestHandler(String name) {
        return rMap.get(name);
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        return EndPoint.CHECK_POINT_DELTA;
    }

    @Override
    public void run() {
        rMap.forEach((k,v)->{
            v.onCheck();
        });
    }
}
