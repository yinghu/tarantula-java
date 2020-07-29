package com.tarantula.platform.service;


import com.tarantula.Descriptor;
import com.tarantula.Event;
import com.tarantula.OnView;
import com.tarantula.platform.presence.GameCluster;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    Batch query(int registryId,String[] params);

    Batch query(String batchId,int count);

    boolean addLobby(Descriptor lobby);

    String addApplication(Descriptor application);
    boolean launchApplication(String typeId,String applicationId);
    boolean shutdownApplication(String typeId,String applicationId);
    boolean resetModule(Descriptor descriptor);

    //add view via typeId of lobby
    boolean addView(OnView view);
    boolean updateView(OnView onView);
    //update lobby or application to set disabled as true/false
    String enableApplication(String applicationId,boolean enabled);
    boolean enableLobby(String typeId,boolean enabled);

    GameCluster createGameCluster(String owner,String name);
    boolean enableGameCluster(String gameClusterId);
    boolean disableGameCluster(String gameClusterId);

    boolean launchGameCluster(String gameClusterKey);
    boolean shutdownGameCluster(String gameClusterKey);

    boolean addServerPushEvent(Event serverPushEvent);
    boolean removeServerPushEvent(String serverId);

    //distribute the module or view content in cluster
    boolean upload(String fileName,byte[] content);

    boolean launchModule(String typeId);
    boolean shutdownModule(String typeId);
    boolean updateModule(Descriptor descriptor);
}
