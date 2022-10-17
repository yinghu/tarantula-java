package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface Metadata extends Recoverable {

     String typeId();
     String source();
     int factoryId();
     int classId();
     int partition();

     Metadata fromRevision(long revision);
}
