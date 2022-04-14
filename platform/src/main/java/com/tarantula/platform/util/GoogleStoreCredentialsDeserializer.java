package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.service.*;

import java.lang.reflect.Type;
import java.util.HashMap;


public class GoogleStoreCredentialsDeserializer implements JsonDeserializer<AuthObject> {

    @Override
    public AuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray ja = jsonElement.getAsJsonObject().getAsJsonArray("validators");
        HashMap<String, GoogleStorePurchaseValidator> _validators = new HashMap<>();
        ja.forEach(a->{
            String typeId = a.getAsJsonObject().get("typeId").getAsString();
            String accessKey = a.getAsJsonObject().get("access_key").getAsString();
            JsonObject android = a.getAsJsonObject().get("android").getAsJsonObject();
            String packageName = android.get("package_name").getAsString();
            String validationUri = android.get("validation_uri").getAsString();
            //JsonObject jo = a.getAsJsonObject().get("web").getAsJsonObject();
            //String clientId = jo.getAsJsonPrimitive("client_id").getAsString();
            //String secureKey = jo.getAsJsonPrimitive("client_secret").getAsString();
            //String authUri = jo.getAsJsonPrimitive("auth_uri").getAsString();
            //String tokenUri = jo.getAsJsonPrimitive("token_uri").getAsString();
            //String certUri = jo.getAsJsonPrimitive("auth_provider_x509_cert_url").getAsString();
            _validators.put(typeId,new GoogleStorePurchaseValidator(typeId,validationUri,packageName,accessKey));
        });
        return new GooglePlayStoreProvider(_validators);
    }

}
