package com.icodesoftware.util;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.icodesoftware.JsonStreamingHandler;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonStreaming {
    private final JsonReader jsonReader;
    private final JsonStreamingHandler jsonStreamingHandler;

    private JsonStreaming(InputStream inputStream, JsonStreamingHandler jsonStreamingHandler){
        jsonReader = new JsonReader(new InputStreamReader(inputStream));
        this.jsonStreamingHandler = jsonStreamingHandler;
    }

    private boolean handle(){
        try(jsonReader){
            JsonToken start = jsonReader.peek();
            if(start != JsonToken.BEGIN_OBJECT) throw new RuntimeException("json object start only");
            handleObject();
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    private void handleObject() throws Exception{
        jsonStreamingHandler.onBeginObject(jsonReader.getPath());
        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            JsonToken token = jsonReader.peek();
            switch (token){
                case NAME -> handleName();

                case BEGIN_OBJECT -> handleObject();

                case BEGIN_ARRAY -> handleArray();

            }
        }
        jsonReader.endObject();
        jsonStreamingHandler.onEndObject(jsonReader.getPath());
    }

    private void handleArray() throws Exception{
        jsonStreamingHandler.onBeginArray(jsonReader.getPath());
        jsonReader.beginArray();
        while (jsonReader.hasNext()){
            JsonToken token = jsonReader.peek();
            switch (token){
                case BEGIN_OBJECT -> handleObject();
                default -> handleValue(jsonReader.getPath());
            }
        }
        jsonReader.endArray();
        jsonStreamingHandler.onEndArray(jsonReader.getPath());
    }

    private void handleName() throws Exception{
        jsonReader.nextName();
        JsonToken tp = jsonReader.peek();
        if(tp == JsonToken.NUMBER || tp == JsonToken.STRING || tp == JsonToken.BOOLEAN || tp == JsonToken.NULL){
            this.handleValue(jsonReader.getPath());
        }
    }

    private void handleValue(String tag) throws Exception{
        JsonToken token = jsonReader.peek();
        switch (token){
            case BOOLEAN -> this.jsonStreamingHandler.onBoolean(tag,jsonReader.nextBoolean());
            case STRING -> this.jsonStreamingHandler.onString(tag,jsonReader.nextString());
            case NUMBER -> this.jsonStreamingHandler.onNumber(tag,Double.valueOf(jsonReader.nextDouble()));
            case NULL -> {
                jsonReader.nextNull();
                this.jsonStreamingHandler.onNull(tag);
            }
        }
    }

    public static boolean handle(InputStream inputStream,JsonStreamingHandler jsonStreamingHandler){
        return new JsonStreaming(inputStream,jsonStreamingHandler).handle();
    }




}
