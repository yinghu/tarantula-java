package com.tarantula.platform.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.icodesoftware.service.ServiceContext;

import java.util.Collections;
import java.util.Map;

public class GoogleOAuthTokenValidator extends AuthObject {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    public GoogleOAuthTokenValidator(String typeId,String clientId, String secureKey, String authUri, String tokenUri, String certUri) {
        super(typeId, clientId, secureKey, authUri, tokenUri, certUri, new String[0]);
        final NetHttpTransport transport = new NetHttpTransport();
        final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport,jsonFactory)
                .setAudience(Collections.singletonList(this.clientId()))
                .setIssuer("https://accounts.google.com").build();
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
    }
    @Override
    public boolean validate(Map<String,Object> params) {
        try{
            final NetHttpTransport transport = new NetHttpTransport();
            final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            String token = (String) params.get("token");
            GoogleAuthorizationCodeTokenRequest request =
                    new GoogleAuthorizationCodeTokenRequest(transport,jsonFactory,tokenUri(),clientId(),secureKey(),token,"");
            GoogleTokenResponse response = request.execute();
            Credential credential = new Credential
                    .Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setJsonFactory(jsonFactory)
                    .setTransport(transport)
                    .setTokenServerEncodedUrl("https://www.googleapis.com/oauth2/v4/token")
                    .setClientAuthentication((req)->{})
                    .build()
                    .setFromTokenResponse(response);
            return credential.getAccessToken()!=null;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean _validate(Map<String,Object> params){
        try{
            //new GoogleIdTokenVerifier.Builder()
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
            metricsListener.onUpdated(Metrics.GOOGLE_COUNT,1);
            System.out.println(payload.getEmail());
            return email!=null;
        }catch (Exception ex){
            ex.printStackTrace();
            return true;
        }
    }

}
