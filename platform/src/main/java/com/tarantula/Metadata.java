package com.tarantula;

import com.icodesoftware.Recoverable;

/**
 * Updated by yinghu lu on 7/8/2020
 */
public interface Metadata extends Recoverable {

     String source();
     int factoryId();
     int classId();
     int partition();
}
