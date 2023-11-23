package com.tarantula.cci.udp;

import com.icodesoftware.Distributable;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnExchange;
import com.tarantula.cci.PendingOperation;


import java.io.File;
import java.io.FileInputStream;

public class DataBootstrapOperation implements PendingOperation {

    private DeploymentServiceProvider deploymentServiceProvider;
    public DataBootstrapOperation(DeploymentServiceProvider deploymentServiceProvider){
        this.deploymentServiceProvider = deploymentServiceProvider;
    }
    @Override
    public void execute(OnExchange exchange) throws Exception{
        File file = this.deploymentServiceProvider.issueDataStoreBackup(Distributable.INDEX_SCOPE);
        FileInputStream fileInputStream = new FileInputStream(file);
        exchange.onStream(fileInputStream);
    }
}
