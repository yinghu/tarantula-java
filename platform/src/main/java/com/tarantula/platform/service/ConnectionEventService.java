package com.tarantula.platform.service;

import com.icodesoftware.Connection;
import com.icodesoftware.service.EventService;


/**
 * Created by yinghu lu on 9/28/2020.
 */
public interface ConnectionEventService extends EventService {
    void publish(byte[] payload,String label, Connection connection);
}
