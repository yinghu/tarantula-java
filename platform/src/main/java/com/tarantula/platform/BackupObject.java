package com.tarantula.platform;

abstract public class BackupObject extends RecoverableObject {
    @Override
    public boolean backup(){
        return true;
    }
    @Override
    public boolean distributable(){return true;}
}
