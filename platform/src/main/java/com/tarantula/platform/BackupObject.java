package com.tarantula.platform;
import com.icodesoftware.util.RecoverableObject;

abstract public class BackupObject extends RecoverableObject {
    @Override
    public boolean backup(){
        return true;
    }
    @Override
    public boolean distributable(){return true;}
}
