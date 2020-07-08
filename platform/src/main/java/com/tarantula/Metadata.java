package com.tarantula;

/**
 * Updated by yinghu lu on 7/8/2020
 */
public interface Metadata extends Recoverable {

     String source();
     int factoryId();
     int classId();
     //int version();
     //int scope();
     int partition();
     //long timestamp();
     //boolean onEdge();
     //boolean distributable(); //set key/value on cluster
     //String index();
}
