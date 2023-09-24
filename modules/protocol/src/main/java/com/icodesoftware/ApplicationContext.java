package com.icodesoftware;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceProvider;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface ApplicationContext extends Context{

    Lobby lobby(String typeId);
    List<Lobby> index();

    Presence presence(Session session);

    void absence(Session session);


    Configuration configuration(String name);

    TokenValidator validator();
    Descriptor descriptor();

    DataStore dataStore(String name);

    RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener);
    void unregisterRecoverableListener(int factoryId);

    <T extends ServiceProvider> T serviceProvider(String name);

    void resource(String name,Module.OnResource onResource);

    PostOffice postOffice();

    ClusterProvider clusterProvider();

    Metrics metrics(String name);


    ClusterProvider.Node node();

    Transaction transaction();
}
