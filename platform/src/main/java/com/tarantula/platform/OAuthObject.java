package com.tarantula.platform;

import com.tarantula.TokenValidator;
import java.util.Map;

/**
 * Created by yinghu lu on 1/31/2019.
 */
public class OAuthObject implements TokenValidator.OAuthVendor {

    private final String name;
    private final String clientId;
    private final String secureKey;
    private final String authUri;
    private final String tokenUri;
    private final String certUri;
    private final String[] origins;

    public OAuthObject(String name,String clientId,String secureKey,String authUri,String tokenUri,String certUri,String[] origins){
        this.name = name;
        this.clientId = clientId;
        this.secureKey = secureKey;
        this.authUri = authUri;
        this.tokenUri = tokenUri;
        this.certUri = certUri;
        this.origins = origins;
    }

    @Override
    public String name(){
        return this.name;
    }
    @Override
    public String clientId() {
        return this.clientId;
    }

    @Override
    public String secureKey() {
        return this.secureKey;
    }

    @Override
    public String authUri() {
        return this.authUri;
    }

    @Override
    public String tokenUri() {
        return this.tokenUri;
    }

    @Override
    public String certUri() {
        return this.certUri;
    }

    @Override
    public String[] origins() {
        return this.origins;
    }

    @Override
    public boolean validate(Map<String,Object> params){
        return false;
    }
}
