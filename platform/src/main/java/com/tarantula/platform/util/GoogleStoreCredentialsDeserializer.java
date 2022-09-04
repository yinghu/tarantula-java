package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class GoogleStoreCredentialsDeserializer implements JsonDeserializer<AuthVendorRegistry> {

    @Override
    public AuthVendorRegistry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray ja = jsonElement.getAsJsonObject().getAsJsonArray("validators");
        List<TokenValidatorProvider.AuthVendor> _validators = new ArrayList<>();

        ja.forEach(a->{
            String typeId = a.getAsJsonObject().get("typeId").getAsString();
            String accessKey = a.getAsJsonObject().get("access_key").getAsString();
            JsonObject android = a.getAsJsonObject().get("android").getAsJsonObject();
            String packageName = android.get("package_name").getAsString();
            //String validationUri = android.get("validation_uri").getAsString();
            _validators.add(new GoogleStorePurchaseValidator(typeId,packageName,accessKey));
        });
        return new ThirdPartyServiceProvider(OnAccess.GOOGLE_STORE,_validators);
    }

}
