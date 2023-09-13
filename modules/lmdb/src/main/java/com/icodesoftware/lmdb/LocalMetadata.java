package com.icodesoftware.lmdb;

import com.icodesoftware.service.Metadata;


public class LocalMetadata  implements Metadata {
    private int scope;
    private String name;
    public LocalMetadata(int scope,String name){
        this.name = name;
        this.scope = scope;
    }
    @Override
    public String typeId() {
        return null;
    }

    @Override
    public String source() {
        return name;
    }

    @Override
    public int scope() {
        return scope;
    }

    @Override
    public int factoryId() {
        return 0;
    }

    @Override
    public int classId() {
        return 0;
    }

    @Override
    public int partition() {
        return 0;
    }


}
