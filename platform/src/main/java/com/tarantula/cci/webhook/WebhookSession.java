package com.tarantula.cci.webhook;

import com.tarantula.Event;
import com.tarantula.cci.OnExchange;

/**
 * Created by yinghu lu on 9/19/2020.
 */
public class WebhookSession implements OnExchange {
    @Override
    public String id() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public String method() {
        return null;
    }

    @Override
    public String header(String name) {
        return null;
    }

    @Override
    public byte[] payload() {
        return new byte[0];
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
