package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class AmazonAuthCredentialsDeserializer implements JsonDeserializer<AuthVendorRegistry> {

    @Override
    public AuthVendorRegistry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray ja = jsonElement.getAsJsonObject().getAsJsonArray("validators");
        List<TokenValidatorProvider.AuthVendor> _validators = new ArrayList<>();
        ja.forEach(a->{
            String typeId = a.getAsJsonObject().get("typeId").getAsString();
            JsonObject iam = a.getAsJsonObject().get("iam").getAsJsonObject();
            String region = iam.get("region").getAsString();
            String bucket = iam.get("bucket").getAsString();
            String accessKeyId = iam.get("access_key_id").getAsString();
            String secureKey = iam.get("secret_access_key").getAsString();
            _validators.add(new AmazonAWSProvider(typeId,region,bucket,accessKeyId,secureKey));
        });
        return new ThirdPartyServiceProvider(OnAccess.AMAZON,_validators);
    }
}
