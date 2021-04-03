package com.tarantula.platform.service;

import com.icodesoftware.Connection;
import com.icodesoftware.service.EventService;

public interface ConnectionEventService extends EventService {
    void publish(byte[] payload,String label, Connection connection);
}
