package com.tarantula.platform;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinghu lu on 1/31/2019.
 */
public class GoogleOAuthProvider extends OAuthObject {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public GoogleOAuthProvider(String clientId, String secureKey, String authUri, String tokenUri, String certUri, String[] origins) {
        super("google", clientId, secureKey, authUri, tokenUri, certUri, origins);
        final NetHttpTransport transport = new NetHttpTransport();
        final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport,jsonFactory).setAudience(Collections.singletonList(this.clientId())).build();
    }

    @Override
    public boolean validate(Map<String,Object> params){
        try{
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(params.get("token").toString());
            if(googleIdToken==null){
                return false;
            }
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            //boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            params.put("email",email);
            params.put("name",name);
            params.put("pictureUrl",pictureUrl);
            params.put("fullName",givenName+" "+familyName);
            return email!=null;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}
