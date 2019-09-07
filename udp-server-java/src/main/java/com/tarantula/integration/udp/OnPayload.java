package com.tarantula.integration.udp;

import com.google.gson.JsonObject;

public interface OnPayload {
    void on(JsonObject jsonObject);
}
