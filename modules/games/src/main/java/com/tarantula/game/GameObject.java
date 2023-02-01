package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.icodesoftware.Response;
import com.icodesoftware.util.*;

import java.lang.reflect.Type;

public class GameObject extends RecoverableObject implements Response {

    protected int stub;
    protected String systemId;

    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        return new JsonObject();
    }

    @Override
    public String command() {
        return null;
    }

    @Override
    public void command(String command) {

    }

    @Override
    public int code() {
        return 0;
    }

    @Override
    public void code(int code) {

    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public void message(String message) {

    }

    @Override
    public boolean successful() {
        return false;
    }

    @Override
    public void successful(boolean successful) {

    }
}
