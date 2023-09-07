package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;
import com.icodesoftware.util.SnowflakeKey;

public class VersionedConfigurableObjectQuery implements RecoverableFactory<ConfigurableObject> {

    public long itemId;
    public VersionedConfigurableObjectQuery(long itemId){
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



    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(itemId);
    }
}
