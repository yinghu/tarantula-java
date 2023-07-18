package com.tarantula.platform.configuration;

import com.icodesoftware.service.ServiceContext;

public interface VendorValidator {

    default boolean validate(ServiceContext serviceContext){ return true;}
}
