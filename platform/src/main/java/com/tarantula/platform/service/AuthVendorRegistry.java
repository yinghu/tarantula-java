package com.tarantula.platform.service;

import com.icodesoftware.service.TokenValidatorProvider;

public interface AuthVendorRegistry extends TokenValidatorProvider.AuthVendor {

    void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor);
    void releaseAuthVendor(TokenValidatorProvider.AuthVendor authVendor);
}
