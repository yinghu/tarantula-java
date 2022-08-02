package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.service.*;

import java.lang.reflect.Type;
import java.util.HashMap;


public class AmazonAuthCredentialsDeserializer implements JsonDeserializer<AuthObject> {

    @Override
    public AuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray ja = jsonElement.getAsJsonObject().getAsJsonArray("validators");
        HashMap<String, AmazonAWSProvider> _validators = new HashMap<>();
        ja.forEach(a->{
            String typeId = a.getAsJsonObject().get("typeId").getAsString();
            JsonObject iam = a.getAsJsonObject().get("iam").getAsJsonObject();
            String region = iam.get("region").getAsString();
            String bucket = iam.get("bucket").getAsString();
            String accessKeyId = iam.get("access_key_id").getAsString();
            String secureKey = iam.get("secret_access_key").getAsString();
            _validators.put(typeId,new AmazonAWSProvider(region,bucket,accessKeyId,secureKey));
        });
        return new AmazonServiceProvider(_validators);
    }
}
