package com.tarantula.platform.service.deployment;

import com.icodesoftware.Configurable;
import com.icodesoftware.service.OnLobby;

public class OnLobbyListener implements Configurable.Listener<OnLobby>{

    private PlatformDeploymentServiceProvider platformDeploymentServiceProvider;
    public OnLobbyListener(PlatformDeploymentServiceProvider platformDeploymentServiceProvider){
        this.platformDeploymentServiceProvider = platformDeploymentServiceProvider;
    }
    @Override
    public void onUpdated(OnLobby onLobby){
        platformDeploymentServiceProvider.register(onLobby);
    }
}
