package com.icodesoftware.lmdb;

import com.icodesoftware.service.Metadata;


public class LocalMetadata  implements Metadata {
    private int scope;
    private String name;

    private String label;
    public LocalMetadata(int scope,String name){
        this.name = name;
        this.scope = scope;
    }
    public LocalMetadata(int scope,String name,String label){
        this.name = name;
        this.scope = scope;
        this.label = label;
    }
    @Override
    public String label() {
        return label;
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

    public String toString(){
        return "Name ["+name+"] Scope ["+scope+"] Label ["+label+"]";
    }

}
