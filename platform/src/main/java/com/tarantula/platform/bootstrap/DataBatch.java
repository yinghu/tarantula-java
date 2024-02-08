package com.tarantula.platform.bootstrap;

public class DataBatch {
    public String fileName;
    public int batch;
    public int remaining;

    public DataBatch(String fileName,int batch,int remaining){
        this.fileName = fileName;
        this.batch = batch;
        this.remaining = remaining;
    }
}
