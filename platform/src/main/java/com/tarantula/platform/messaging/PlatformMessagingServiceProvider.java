package com.tarantula.platform.messaging;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.service.GameServiceProvider;


public class PlatformMessagingServiceProvider implements ServiceProvider {

    public static final String NAME = "messaging";
    private static final int MESSAGE_OBJECT_ID = 0;
    private final GameServiceProvider gameServiceProvider;
    private String topic;
    private ServiceContext serviceContext;
    private TarantulaLogger logger;


    public PlatformMessagingServiceProvider(GameServiceProvider gameServiceProvider){
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

    public void publish(byte[] message){
        this.serviceContext.postOffice().onTopic(topic).send(NAME,messageHeader(),message);
    }

    private MessageBuffer.MessageHeader messageHeader(){
        MessageBuffer.MessageHeader header = new MessageBuffer.MessageHeader();
        header.objectId = MESSAGE_OBJECT_ID;
        header.sequence = 1;
        return header;
    }
}
