package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Response;
import com.tarantula.platform.ResponseHeader;

import java.lang.reflect.Type;


/**
 * Updated by yinghu on 8/26/19
 */
public class ResponseDeserializer implements JsonDeserializer<Response> {

    private Response response;

    public ResponseDeserializer(){}


    @Override
    public Response deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(response==null){
            response = new ResponseHeader();
        }
        JsonObject jo = jsonElement.getAsJsonObject();
        if(jo.has("command")){
            response.command(jo.get("command").getAsString());
        }
        if(jo.has("code")){
            response.code(jo.get("code").getAsInt());
        }
        if(jo.has("successful")){
            response.successful(jo.get("successful").getAsBoolean());
        }
        if(jo.has("message")){
            response.message(jo.get("message").getAsString());
        }
        if(jo.has("disabled")){
            response.disabled(jo.get("disabled").getAsBoolean());
        }
        return this.response;
    }
}
