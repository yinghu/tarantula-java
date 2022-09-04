package com.tarantula.platform.util;

import com.google.gson.*;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class GoogleAuthCredentialsDeserializer implements JsonDeserializer<AuthVendorRegistry> {

    @Override
    public AuthVendorRegistry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray ja = jsonElement.getAsJsonObject().getAsJsonArray("validators");
        List<TokenValidatorProvider.AuthVendor> _validators = new ArrayList<>();
        ja.forEach(a->{
            String typeId = a.getAsJsonObject().get("typeId").getAsString();
            String accessKey = a.getAsJsonObject().get("access_key").getAsString();
            JsonObject android = a.getAsJsonObject().get("android").getAsJsonObject();
            String applicationId = android.get("application_id").getAsString();
            //String verifyUri = android.get("verify_uri").getAsString();
            JsonObject jo = a.getAsJsonObject().get("web").getAsJsonObject();
            String clientId = jo.getAsJsonPrimitive("client_id").getAsString();
            String secureKey = jo.getAsJsonPrimitive("client_secret").getAsString();
            //String authUri = jo.getAsJsonPrimitive("auth_uri").getAsString();
            //String tokenUri = jo.getAsJsonPrimitive("token_uri").getAsString();
            //String certUri = jo.getAsJsonPrimitive("auth_provider_x509_cert_url").getAsString();
            _validators.add(new GoogleOAuthTokenValidator(typeId,clientId,secureKey,applicationId,accessKey));
        });
        return new ThirdPartyServiceProvider(OnAccess.GOOGLE,_validators);
    }

}
