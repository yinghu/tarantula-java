package com.icodesoftware.service;

import com.icodesoftware.*;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    boolean onUpdateView(OnView onView);
    boolean onUpdateResource(String contentUrl,String resourceName);


    boolean onLaunchApplication(String typeId,long applicationId);
    boolean onShutdownApplication(String typeId,long applicationId);


    boolean onStartGameService(long gameClusterKey);
    boolean onLaunchGameCluster(long gameClusterKey);
    boolean onShutdownGameCluster(long gameClusterKey);

    //distribute the module or view content in cluster
    boolean onUpload(String fileName,byte[] content);

    void onVerifyConnection(String typeId,String serverId);
    void onRegisterConnection(Connection connection);
    void onStartConnection(Connection connection);
    void onReleaseConnection(Connection connection);

    byte[] onClusterKey();

    void onResetClusterKey();

    byte[] onTokenKey();

    void onResetTokenKey();

    void onEnablePresenceService(String root,String password,String classNameSuffix,String host);
    void onDisablePresenceService(String classNameSuffix);

    void onIssueDataStoreBackup(int scope);

}
