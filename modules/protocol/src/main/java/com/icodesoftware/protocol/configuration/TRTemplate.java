package com.icodesoftware.protocol.configuration;

import com.icodesoftware.service.Template;

import java.util.ArrayList;
import java.util.List;

public class TRTemplate implements Template{

    protected String scope;
    protected String type;
    protected String version;
    protected boolean rechargeable;
    protected List<Template.Property> properties = new ArrayList<>();

    @Override
    public String scope() {
        return scope;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public boolean rechargeable() {
        return rechargeable;
    }

    @Override
    public List<Property> properties() {
        return properties;
    }

    public void scope(String scope){
        this.scope = scope;
    }

    public void type(String type){
        this.type = type;
    }
    public void version(String version){
        this.version = version;
    }
    public void rechargeable(boolean rechargeable){
        this.rechargeable = rechargeable;
    }

    public void property(Property property){
        properties.add(property);
    }
}
