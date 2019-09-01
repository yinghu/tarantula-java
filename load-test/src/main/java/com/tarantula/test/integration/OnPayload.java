package com.tarantula.test.integration;

import com.google.gson.JsonObject;

public interface OnPayload {
    void on(JsonObject jsonObject);
}
