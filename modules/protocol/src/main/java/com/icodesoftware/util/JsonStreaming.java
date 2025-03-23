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

    private boolean handleObject() throws Exception{
        jsonStreamingHandler.onBeginObject(jsonReader.getPath(),index(jsonReader.getPath()));
        jsonReader.beginObject();
        boolean stopLoop = false;
        while (jsonReader.hasNext()){
            if(stopLoop) break;
            JsonToken token = jsonReader.peek();
            switch (token){
                case NAME -> stopLoop = handleName();

                case BEGIN_OBJECT -> stopLoop = handleObject();

                case BEGIN_ARRAY -> stopLoop = handleArray();

            }
        }
        if(stopLoop) return true;
        jsonReader.endObject();
        return jsonStreamingHandler.onEndObject(jsonReader.getPath(),index(jsonReader.getPath()));
    }

    private boolean handleArray() throws Exception{
        jsonStreamingHandler.onBeginArray(jsonReader.getPath(),index(jsonReader.getPath()));
        jsonReader.beginArray();
        boolean stopLoop = false;
        while (jsonReader.hasNext()){
            if(stopLoop) break;
            JsonToken token = jsonReader.peek();
            switch (token){
                case BEGIN_OBJECT -> stopLoop = handleObject();
                default -> stopLoop = handleValue(jsonReader.getPath());
            }
        }
        if(stopLoop) return true;
        jsonReader.endArray();
        return jsonStreamingHandler.onEndArray(jsonReader.getPath(),index(jsonReader.getPath()));
    }

    private boolean handleName() throws Exception{
        boolean stopLoop = false;
        jsonReader.nextName();
        JsonToken tp = jsonReader.peek();
        if(tp == JsonToken.NUMBER || tp == JsonToken.STRING || tp == JsonToken.BOOLEAN || tp == JsonToken.NULL){
            stopLoop = this.handleValue(jsonReader.getPath());
        }
        return stopLoop;
    }

    private boolean handleValue(String tag) throws Exception{
        JsonToken token = jsonReader.peek();
        boolean stopLoop = false;
        switch (token){
            case BOOLEAN -> stopLoop = this.jsonStreamingHandler.onBoolean(tag,jsonReader.nextBoolean(),index(tag));

            case STRING -> stopLoop = this.jsonStreamingHandler.onString(tag,jsonReader.nextString(),index(tag));
            case NUMBER -> stopLoop = this.jsonStreamingHandler.onNumber(tag,Double.valueOf(jsonReader.nextDouble()),index(tag));
            case NULL -> {
                jsonReader.nextNull();
                stopLoop = this.jsonStreamingHandler.onNull(tag,index(tag));
            }
        }
        return stopLoop;
    }

    private int index(String path){
        int ix = path.lastIndexOf("[");
        if(ix == -1) return -1;
        return Integer.parseInt(path.substring(ix+1,ix+2));
    }

    public static boolean handle(InputStream inputStream,JsonStreamingHandler jsonStreamingHandler){
        return new JsonStreaming(inputStream,jsonStreamingHandler).handle();
    }




}
