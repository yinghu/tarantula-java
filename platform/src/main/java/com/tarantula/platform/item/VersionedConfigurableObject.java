package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.AssociateKey;

public class VersionedConfigurableObject extends ConfigurableObject{

    public static final String LABEL = "version";

    public VersionedConfigurableObject(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public VersionedConfigurableObject(ConfigurableObject configurableObject){
        this();
        this.ownerKey = configurableObject.key();
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
        this.distributionId(configurableObject.distributionId());
    }
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.VERSIONED_CONFIGURABLE_OBJECT_CID;
    }

    public boolean readKey(Recoverable.DataBuffer buffer){
        distributionId = buffer.readLong();
        configurationVersion = buffer.readUTF8();
        return true;
    }
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(distributionId==0) return false;
        buffer.writeLong(distributionId);
        buffer.writeUTF8(configurationVersion);
        return true;
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.distributionId,this.configurationVersion);
    }

}
