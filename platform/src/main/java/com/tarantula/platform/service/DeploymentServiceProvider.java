package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.Module;

import java.io.InputStream;

/**
 * Updated by yinghu lu on 6/15/2019.
 */

public interface DeploymentServiceProvider extends ServiceProvider {

    String DEPLOY_TOPIC = "tarantula-deployment";
    String DEPLOY_DATA_STORE = "tarantula";

    String NAME = "DeploymentServiceProvider";

    void clusterUpdated(int scope,String nodeId,boolean state);

    //UDP SERVER APIs
    void onUDPConnection(String typeId,Connection connection);
    Connection onUDPConnection(String typeId, Connection.ConnectionEndedListener listener);
    void onStartedUDPConnection(String serverId,byte[] started);
    void onEndedUDPConnection(String serverId,byte[] ended);
    byte[] onStartedUDPConnection(String serverId);
    //END OF DEDICATED SERVER APIs

    /**
     * Deploys the service provider on service pool
     * */
    boolean deploy(ServiceProvider serviceProvider);

    //deploy and callback instance registry
    void registerInstanceRegistryListener(InstanceRegistry.Listener deploymentListener);
    void deploy(InstanceRegistry registry);

    void registerOnConnectionListener(Connection.Listener listener);
    //deploy and callback configuration
    void deploy(Configuration configuration);
    void registerConfigurationListener(Configuration.Listener listener);

    //deploy and callback on view
    void deploy(OnView onView);
    void registerOnViewListener(OnView.Listener onViewListener);

    //deploy and callback on lobby
    void deploy(OnLobby onLobby);

    void registerOnLobbyListener(OnLobby.Listener onLobbyListener);

    byte[] resource(String name,String flag);

    //message publisher
    PostOffice registerPostOffice();

    //Module application operation API
    String upload(InputStream inputStream,String fname) throws Exception;
    String createLobby(Descriptor descriptor);
    String createApplication(Descriptor descriptor);
    String enableApplication(String applicationId,boolean enabled);
    String launch(String typeId);
    String shutdown(String typeId);
    Module module(Descriptor descriptor);
    void resource(Descriptor descriptor, String name, Module.OnResource onResource);
    String reset(Descriptor descriptor);
    String createModule(Descriptor descriptor);
    //END OF MODULE OPERATION API

    DataStoreProvider dataStoreProvider();


}
