package com.tarantula;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface ApplicationContext{

    Lobby lobby(String typeId);
    List<Lobby> index();

    Presence presence(String systemId);

    void absence(Session session);

    ScheduledFuture<?> schedule(SchedulingTask task);

    InstanceRegistry onRegistry();

    Configuration configuration(String type);
    List<Configuration> configuration();

    TokenValidator validator();

    Descriptor descriptor(String applicationId);

    Statistics onStatistics();

    DataStore dataStore(String name);

    <T extends Recoverable> void publish(RoutingKey routingKey,T t);
    RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener);

    void log(String message,int level);
    void log(String message,Exception error,int level);

    <T extends ServiceProvider> T serviceProvider(String name);

    RoutingKey instanceRoutingKey(String application,String instanceId);
    RoutingKey routingKey(String magicKey,String tag);
    RoutingKey routingKey(String magicKey,String tag,int routingNumber);

    //total partition number
    int routingNumber();

    void resource(String name,Module.OnResource onResource);

    PostOffice postOffice();
}
