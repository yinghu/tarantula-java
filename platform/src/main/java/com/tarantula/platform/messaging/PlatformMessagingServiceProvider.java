package com.tarantula.platform.messaging;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.service.PlatformGameServiceProvider;

public class PlatformMessagingServiceProvider implements ServiceProvider {

    public static final String NAME = "messaging";

    private final PlatformGameServiceProvider gameServiceProvider;

    private String topic;

    private ServiceContext serviceContext;

    private TarantulaLogger logger;


    public PlatformMessagingServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("Messaging service started on ["+topic+"]");
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.logger = this.serviceContext.logger(PlatformMessagingServiceProvider.class);
        topic = this.gameServiceProvider.registerEventListener(NAME,e->{
            return true;
        });
    }

    public void publish(Session.Header header,byte[] message){
        this.serviceContext.postOffice().onTopic(topic).send(NAME,header,message);
    }
}
