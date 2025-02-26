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

    //PostOffice postOffice();
    //default PostOffice postOffice(Session session){ throw new UnsupportedOperationException();}

    ClusterProvider clusterProvider();

    void onMetrics(String category,double delta);


    ClusterProvider.Node node();

    Transaction transaction();

    default Transaction.LogManager logManager(){ throw new UnsupportedOperationException();}
    default DataStore dataStore(int scope,String name){
        return dataStore(name);
    }
    default long distributionId(){
        return 0;
    }
    default Transaction transaction(int scope){ throw new UnsupportedOperationException();}
}
