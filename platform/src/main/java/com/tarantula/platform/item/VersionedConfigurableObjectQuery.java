package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;

public class VersionedConfigurableObjectQuery implements RecoverableFactory<ConfigurableObject> {

    public String itemId;
    public VersionedConfigurableObjectQuery(String itemId){
        this.itemId = itemId;

    }

    @Override
    public ConfigurableObject create() {
        return new VersionedConfigurableObject();
    }

    @Override
    public int registryId() {
        return ItemPortableRegistry.VERSIONED_CONFIGURABLE_OBJECT_CID;
    }

    @Override
    public String label() {
        return VersionedConfigurableObject.LABEL;
    }


    public String distributionKey() {
        return itemId;
    }

    @Override
    public Recoverable.Key key() {
        return new OidKey(itemId);
    }
}
