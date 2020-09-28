package com.tarantula.platform.service;

import com.tarantula.Connection;
import com.tarantula.Event;
import com.tarantula.EventService;


/**
 * Created by yinghu lu on 9/28/2020.
 */
public interface ConnectionEventService extends EventService {
    void publish(Event event, Connection connection);
}
