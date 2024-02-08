package com.tarantula.cci;

import com.icodesoftware.Distributable;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnExchange;
import com.tarantula.cci.PendingOperation;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

public class DataBootstrapOperation implements PendingOperation {

    private DeploymentServiceProvider deploymentServiceProvider;
    private String fileName;
    private int offset;

    private int size;
    public DataBootstrapOperation(DeploymentServiceProvider deploymentServiceProvider,String query){
        this.deploymentServiceProvider = deploymentServiceProvider;
        String[] parts = query.split("#");
        fileName = parts[0];
        offset = Integer.parseInt(parts[1]);
        size = Integer.parseInt(parts[2]);
    }
    @Override
    public void execute(OnExchange exchange) throws Exception{
        RandomAccessFile buffer = new RandomAccessFile(fileName,"r");
        System.out.println("OFFSET : "+offset+" : "+size+" : "+buffer.length());
        byte[] payload = new byte[size];
        buffer.seek(offset);
        buffer.read(payload);
        buffer.close();
        exchange.onStream(new ByteArrayInputStream(payload));
    }
}
