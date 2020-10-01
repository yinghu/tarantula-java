package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class AccessKey extends OnApplicationHeader implements OnAccess {

    public static final String TIMESTAMP ="1";
    public static final String KEY_LABEL = "2";

    public int getFactoryId() {
        return PortableRegistry.OID;
    }


    public int getClassId() {
        return PortableRegistry.ACCESS_KEY;
    }
}
