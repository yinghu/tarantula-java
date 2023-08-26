package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.tarantula.platform.AssociateKey;

public class VersionedConfigurableObject extends ConfigurableObject{

    public VersionedConfigurableObject(){

    }

    public VersionedConfigurableObject(ConfigurableObject configurableObject){
        this.configurationType = configurableObject.configurationType();
        this.configurationTypeId = configurableObject.configurationTypeId();
        this.configurationName = configurableObject.configurationName();
        this.configurationCategory = configurableObject.configurationCategory();
        this.configurationVersion = configurableObject.configurationVersion();
        this.configurationScope = configurableObject.configurationScope;
        this.disabled = configurableObject.disabled();
        this.header = configurableObject.header();
        this.application = configurableObject.application;
        this.reference = configurableObject.reference;
        //this.listener = configurableObject.listener;
        //this._reference = configurableObject._reference;
        this._configurableSetting = configurableObject._configurableSetting;
        this.distributionKey(configurableObject.distributionKey());
    }

    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.id,this.configurationVersion);
    }

    @Override
    public void distributionKey(String distributionKey){
        try{
            String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
            this.bucket = klist[0];
            this.oid = klist[1];
            this.configurationVersion = klist[2];
        }catch (Exception ex){
            //ignore wrong format key
        }
    }
}
