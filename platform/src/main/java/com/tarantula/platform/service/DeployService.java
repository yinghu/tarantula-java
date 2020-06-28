package com.tarantula.platform.service;


import com.tarantula.Descriptor;
import com.tarantula.OnView;
import com.tarantula.platform.presence.GameCluster;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    Batch query(int registryId,String[] params);

    Batch query(String batchId,int count);

    //void recover(String destination,String registerId,boolean fullBackup);

    boolean addLobby(Descriptor lobby);

    String addApplication(Descriptor application);
    boolean resetModule(String lobbyId,Descriptor descriptor);

    //add view via typeId of lobby
    boolean addView(OnView view);

    //update lobby or application to set disabled as true/false
    String enableApplication(String applicationId,boolean enabled);
    boolean enableLobby(String typeId,boolean enabled);

    GameCluster createGameCluster(String owner,String name);
    boolean enableGameCluster(String gameClusterId);
    boolean disableGameCluster(String gameClusterId);
}
