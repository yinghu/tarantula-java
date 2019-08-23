package com.tarantula;

/**
 * Updated by yinghu lu on 8/23/2019.
 */
public interface Metadata extends Recoverable {

     String source();
     int factoryId();
     int classId();
     int version();
     int scope();
     int partition();
     long timestamp();
     boolean onEdge();
     boolean distributable();
     String index();
}
