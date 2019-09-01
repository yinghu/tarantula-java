package com.tarantula.test.integration;

import com.google.gson.JsonObject;

public interface OnResponse {
    void on(JsonObject jsonObject);
}
