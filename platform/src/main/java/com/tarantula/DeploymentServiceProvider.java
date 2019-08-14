package com.tarantula;

/**
 * Updated by yinghu lu on 6/15/2019.
 */

public interface DeploymentServiceProvider extends ServiceProvider{

    String DEPLOY_TOPIC = "tarantula-deployment";

    String NAME = "DeploymentServiceProvider";

    void clusterUpdated(int scope,String nodeId,boolean state);
    /**
     * Deploys the service provider on service pool
     * */
    boolean deploy(ServiceProvider serviceProvider);

    //deploy and callback instance registry
    void registerInstanceRegistryListener(InstanceRegistry.Listener deploymentListener);
    void deploy(InstanceRegistry registry);

    //deploy and callback configuration
    void deploy(Configuration configuration);
    void registerConfigurationListener(Configuration.Listener listener);

    //deploy and callback on view
    void deploy(OnView onView);
    void registerOnViewListener(OnView.Listener onViewListener);

    //deploy and callback on lobby
    void deploy(OnLobby onLobby);
    void registerOnLobbyListener(OnLobby.Listener onLobbyListener);

    //message publisher
    PostOffice registerPostOffice();

    //Module application operation API
    String createLobby(Descriptor descriptor);
    String createApplication(Descriptor descriptor);
    String enableApplication(String applicationId,boolean enabled);
    void launch(String typeId);
    void shutdown(String typeId);
    Module module(Descriptor descriptor);
    void resource(Descriptor descriptor, String name, Module.OnResource onResource);
    void reset(Descriptor descriptor);
    //END OF MODULE OPERATION API


}
