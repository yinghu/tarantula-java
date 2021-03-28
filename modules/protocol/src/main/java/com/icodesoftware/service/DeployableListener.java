package com.icodesoftware.service;

import com.icodesoftware.Deployable;

public interface DeployableListener {
    default boolean validate(Deployable deployable){ return true;}
}
