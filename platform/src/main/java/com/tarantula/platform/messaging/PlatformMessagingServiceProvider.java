package com.tarantula.platform.messaging;

import com.icodesoftware.Channel;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.service.GameServiceProvider;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformMessagingServiceProvider implements ServiceProvider {

    public static final String NAME = "messaging";

    private final GameServiceProvider gameServiceProvider;
    private final String serviceName;
    private ServiceContext serviceContext;
    private TarantulaLogger logger;
    private ConcurrentHashMap<Recoverable.Key,Channel> channelMap;
    public PlatformMessagingServiceProvider(GameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
        this.serviceName = this.gameServiceProvider.gameCluster().serviceType();
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
        this.serviceContext.eventService().registerEventListener(serviceName,e->{
            logger.warn(e.toString());
            return true;
        });
    }

    public void registerChannel(Session session,Channel gameChannel){
        logger.warn("register game channel->"+session.key().asString()+">>>"+gameChannel.sessionId());
        channelMap.put(session.key(),gameChannel);
        //this.serviceContext.postOffice().onTopic().send(serviceName,"{}".getBytes());
        //channelMap.put()
    }
    public void unregisterGameChannel(Session session){
        logger.warn("unregister game channel->"+session.key().asString());
        channelMap.remove(session.key());
    }
}
