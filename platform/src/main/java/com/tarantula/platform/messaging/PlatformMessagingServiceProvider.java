package com.tarantula.platform.messaging;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.service.EventService;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.ScheduleRunner;
import com.tarantula.platform.event.ServerPushEvent;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformMessagingServiceProvider implements ServiceProvider {

    public static final String NAME = "messaging";
    private static final int MESSAGE_OBJECT_ID = 0;
    private final GameServiceProvider gameServiceProvider;
    private final String serviceName;
    private ServiceContext serviceContext;
    private TarantulaLogger logger;
    private ConcurrentHashMap<Recoverable.Key,Channel> channelMap;

    private EventService publisher;

    public PlatformMessagingServiceProvider(GameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
        this.serviceName = this.gameServiceProvider.gameCluster().typeId()+"-"+NAME;
        this.channelMap = new ConcurrentHashMap<>();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("Messaging service started on ["+serviceName+"]");
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.logger = this.serviceContext.logger(PlatformMessagingServiceProvider.class);
        this.publisher = this.serviceContext.clusterProvider().publisher();
        this.publisher.registerEventListener(serviceName,e->{
            this.channelMap.forEach((k,c)->send(c,(ServerPushEvent)e));
            return true;
        });
    }

    public void registerChannel(Session session,Channel gameChannel){
        logger.warn("register game channel->"+session.key().asString()+">>>"+gameChannel.sessionId());
        channelMap.put(session.key(),gameChannel);
        this.serviceContext.schedule(new ScheduleRunner(3000,()-> {
            Statistics statistics = this.gameServiceProvider.statistics(session.systemId());
            publisher.publish(new ServerPushEvent(serviceName,1,statistics.toJson().toString().getBytes()));
        }));
    }

    public void unregisterGameChannel(Session session){
        logger.warn("unregister game channel->"+session.key().asString());
        channelMap.remove(session.key());
    }


    private void send(Channel channel, ServerPushEvent serverPushEvent){
        logger.warn(serverPushEvent.toString());
        MessageBuffer.MessageHeader header = new MessageBuffer.MessageHeader();
        header.objectId = MESSAGE_OBJECT_ID;
        header.sequence = serverPushEvent.stub();//messageSequence.incrementAndGet();
        channel.write(header,serverPushEvent.payload());
    }
}
