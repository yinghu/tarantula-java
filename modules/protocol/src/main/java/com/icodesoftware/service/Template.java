package com.icodesoftware.service;

import java.util.List;

public interface Template {

    String scope();
    String type();
    String version();
    boolean rechargeable();

    List<Property> properties();

    interface Property{
        String name();
        String type();
        String reference();
        boolean downloadable();
    }
}
