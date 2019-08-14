package com.tarantula.platform.presence;

import com.tarantula.Lobby;
import com.tarantula.OnView;
import com.tarantula.TokenValidator;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

/**
 * Updated by yinghu lu on 10/9/2018.
 */
public class IndexContext extends ResponseHeader {

    public OnView view;
    public List<Lobby> lobbyList;
    public List<TokenValidator.OAuthVendor> oAuthVendorList;
}
