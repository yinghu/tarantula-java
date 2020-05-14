package com.tarantula.platform.service;

import com.tarantula.cci.*;
import com.tarantula.platform.TarantulaContext;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Developer: YINGHU LU
 * Date updated 5/5/2020
 */
public class EndpointService implements Serviceable,EndPoint.Resource{

    TarantulaContext tarantulaContext;
    private HashMap<String,RequestHandler> rMap = new HashMap<>();
    private final ArrayList<EndPoint> endPointList;
    private final PushEventHandler pushEventHandler;

    public EndpointService(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
        this.endPointList = new ArrayList<>();
        this.pushEventHandler = new PushEventHandler();
    }

    public void start() throws Exception {
        RootContentHandler rootContentHandler = new RootContentHandler();
        rootContentHandler.setup(this.tarantulaContext);
        rootContentHandler.start();
        rMap.put(rootContentHandler.name(),rootContentHandler);

        UploadEventHandler uploadEventHandler = new UploadEventHandler();
        uploadEventHandler.setup(this.tarantulaContext);
        uploadEventHandler.start();
        rMap.put(uploadEventHandler.name(),uploadEventHandler);

        ResourceEventHandler resourceEventHandler = new ResourceEventHandler();
        resourceEventHandler.setup(this.tarantulaContext);
        resourceEventHandler.start();
        rMap.put(resourceEventHandler.name(),resourceEventHandler);

        UserEventHandler userHandler = new UserEventHandler();
        userHandler.setup(this.tarantulaContext);
        userHandler.start();
        rMap.put(userHandler.name(),userHandler);

        ServiceEventHandler serviceEventHandler = new ServiceEventHandler();
        serviceEventHandler.setup(this.tarantulaContext);
        serviceEventHandler.start();
        rMap.put(serviceEventHandler.name(),serviceEventHandler);

        ApplicationEventHandler applicationHandler = new ApplicationEventHandler();
        applicationHandler.setup(this.tarantulaContext);
        applicationHandler.start();
        rMap.put(applicationHandler.name(),applicationHandler);

        pushEventHandler.setup(this.tarantulaContext);
        pushEventHandler.start();
        rMap.put(pushEventHandler.name(),pushEventHandler);

        DedicatedServerEventHandler dedicatedServerEventHandler = new DedicatedServerEventHandler();
        dedicatedServerEventHandler.setup(this.tarantulaContext);
        dedicatedServerEventHandler.start();
        rMap.put(dedicatedServerEventHandler.name(),dedicatedServerEventHandler);

        AdminEventHandler adminEventHandler = new AdminEventHandler();
        adminEventHandler.setup(this.tarantulaContext);
        adminEventHandler.start();
        rMap.put(adminEventHandler.name(),adminEventHandler);

        ViewEventHandler viewEventHandler = new ViewEventHandler();
        viewEventHandler.setup(this.tarantulaContext);
        viewEventHandler.start();
        rMap.put(viewEventHandler.name(),viewEventHandler);

        for(EndPoint endPoint : endPointList){
            endPoint.resource(this);
            endPoint.start();
            endPoint.setup(this.tarantulaContext);
            endPoint.waitForData();
            this.tarantulaContext.node_started.set(true);
        }
    }

    public void shutdown() throws Exception {
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


    public void atMidnight() {
        rMap.forEach((k,v)->{
            System.out.println("midnight check->"+k);
            v.onCheck();
        });
    }
}
