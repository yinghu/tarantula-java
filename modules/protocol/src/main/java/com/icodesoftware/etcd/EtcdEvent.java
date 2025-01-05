package com.icodesoftware.etcd;

import com.icodesoftware.util.RecoverableObject;

public class EtcdEvent extends RecoverableObject {

    protected String key;
    protected String prefix;
    protected String nodeName;

    @Override
    public Key key() {
        return new WatchKey(prefix,key+"#"+getClassId());
    }

    public String toString(){
        return "Event ["+(prefix!=null?prefix:WatchKey.PREFIX)+":"+key+"]";
    }
}
