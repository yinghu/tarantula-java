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
    Descriptor descriptor();
    Descriptor descriptor(String applicationId);

    Statistics statistics();

    DataStore dataStore(String name);

    RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener);

    void log(String message,int level);
    void log(String message,Exception error,int level);

    <T extends ServiceProvider> T serviceProvider(String name);

    void resource(String name,Module.OnResource onResource);

    PostOffice postOffice();
}
