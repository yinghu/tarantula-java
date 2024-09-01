package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.IntegerKey;


public class ConfigurableEdit extends ConfigurableObject {

    public int configurationId;

    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_EDIT_CID;
    }

    public String configurationScope() {
        return configurationScope;
    }

    public void configurationScope(String configurationScope) {
        this.configurationScope = configurationScope;
    }


    public boolean read(DataBuffer buffer){
        this.configurationType = buffer.readUTF8();
        this.configurationTypeId = buffer.readUTF8();
        this.configurationName = buffer.readUTF8();
        this.configurationCategory = buffer.readUTF8();
        this.configurationVersion = buffer.readUTF8();
        this.configurationScope = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(this.configurationType);
        buffer.writeUTF8(this.configurationTypeId);
        buffer.writeUTF8(this.configurationName);
        buffer.writeUTF8(this.configurationCategory);
        buffer.writeUTF8(this.configurationVersion);
        buffer.writeUTF8(this.configurationScope);
        return true;
    }

    public boolean readKey(Recoverable.DataBuffer buffer){
        configurationId = buffer.readInt();
        return true;
    }
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(configurationId <=0 ) return false;
        buffer.writeInt(configurationId);
        return true;
    }

    @Override
    public Key key() {
        return IntegerKey.from(configurationId);
    }
}
