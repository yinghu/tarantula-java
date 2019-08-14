package com.tarantula;

import com.hazelcast.nio.serialization.Portable;

/**
 * Created by yinghu lu on 9/24/2018.
 */
public interface Metadata extends Portable {

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
