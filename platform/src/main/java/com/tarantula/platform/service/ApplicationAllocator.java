package com.tarantula.platform.service;

import com.tarantula.InstanceRegistry;

/*
 * Updated by yinghu on 8/1/2019
 * None singleton application deployment API
 */
public interface ApplicationAllocator {

    void configure();

    InstanceRegistry allocate(int partition);

}
