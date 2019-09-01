package com.tarantula.test.integration;

import com.google.gson.JsonObject;

import java.net.http.WebSocket;

public interface OnGame {

    String typeId();
    void onPlay(JsonObject joined, WebSocket webSocket,HTTPCaller httpCaller,JsonObject presence);
    void onMessage(CharSequence message);
}
