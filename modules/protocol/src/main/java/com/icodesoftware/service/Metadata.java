package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface Metadata extends Recoverable {

     String source();
     int factoryId();
     int classId();
     int partition();
}
