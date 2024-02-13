package com.tarantula.cci;

import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnExchange;
import com.tarantula.platform.event.ResponsiveEvent;

import java.io.File;
import java.io.FileInputStream;

public class DataBackupOperation implements PendingOperation {

    private DeploymentServiceProvider deploymentServiceProvider;
    public DataBackupOperation(DeploymentServiceProvider deploymentServiceProvider){
        this.deploymentServiceProvider = deploymentServiceProvider;
    }
    @Override
    public void execute(OnExchange exchange) throws Exception{
        File file = this.deploymentServiceProvider.issueDataStoreBackup(Distributable.INDEX_SCOPE);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("file",file.getAbsolutePath());
        jsonObject.addProperty("size",file.length());
        ResponsiveEvent event = new ResponsiveEvent("",exchange.id(),jsonObject.toString().getBytes(),true);
        exchange.onEvent(event);
    }
}
