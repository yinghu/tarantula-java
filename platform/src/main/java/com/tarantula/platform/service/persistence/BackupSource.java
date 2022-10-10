package com.tarantula.platform.service.persistence;

public class BackupSource {

    public final String name;
    public final int scope;
    public final int partition;

    public BackupSource(String name,int scope,int partition){
        this.name = name;
        this.scope = scope;
        this.partition = partition;
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }
    @Override
    public boolean equals(Object object){
        BackupSource backupSource = (BackupSource)object;
        return backupSource.name.equals(name);
    }
}
