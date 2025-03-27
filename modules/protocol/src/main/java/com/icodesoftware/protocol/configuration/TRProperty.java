package com.icodesoftware.protocol.configuration;

import com.icodesoftware.service.Template;

public class TRProperty implements Template.Property {

    protected String name;
    protected String type;
    protected String reference;
    protected boolean downloadable;

    @Override
    public String name() {
        return name;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String reference() {
        return reference;
    }

    @Override
    public boolean downloadable() {
        return downloadable;
    }

    public void name(String name){
        this.name = name;
    }

    public void type(String type){
        this.type = type;
    }
    public void reference(String reference){
        this.reference = reference;
    }
    public void downloadable(boolean downloadable){
        this.downloadable = downloadable;
    }


}
