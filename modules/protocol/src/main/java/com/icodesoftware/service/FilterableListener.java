package com.icodesoftware.service;

import com.icodesoftware.Filterable;

public interface FilterableListener {
    default boolean validate(Filterable deployable){ return true;}
}
