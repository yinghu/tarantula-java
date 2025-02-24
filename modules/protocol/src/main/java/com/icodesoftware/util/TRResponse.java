package com.icodesoftware.util;

import com.google.gson.JsonObject;
import com.icodesoftware.Response;

public class TRResponse extends RecoverableObject implements Response {

    protected String command;
    protected int code;
    protected String message;
    protected boolean successful;

    public TRResponse(){}
    public TRResponse(String command){
        this.command = command;
        this.successful = true;
    }
    public TRResponse(String command, int code){
        this.command = command;
        this.successful = true;
        this.code = code;
    }
    public TRResponse(String command, String message){
        this.command = command;
        this.successful = true;
        this.message = message;
    }
    public TRResponse(String command, String message, boolean successful){
        this.command = command;
        this.successful = successful;
        this.message = message;
    }
    public TRResponse(String command, boolean successful, int code, String message, String label){
        this.command = command;
        this.successful = successful;
        this.code = code;
        this.message = message;
        this.label = label;
    }
    public String command() {
        return command;
    }

    public void command(String command) {
        this.command = command;
    }

    public int code() {
        return code;
    }

    public void code(int code) {
        this.code = code;
    }

    public String message() {
        return message;
    }

    public void message(String message) {
        this.message = message;
    }

    public boolean successful() {
        return successful;
    }
    public void successful(boolean successful) {
        this.successful = successful;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jo = new JsonObject();
        jo.addProperty("command",command);
        jo.addProperty("label",label);
        jo.addProperty("message",message);
        jo.addProperty("successful",successful);
        jo.addProperty("Successful",successful);
        jo.addProperty("Message",message);
        return jo;
    }
}
