package com.tarantula.test.integration;

import com.google.gson.GsonBuilder;
import com.tarantula.OnAccess;
import com.tarantula.Response;
import com.tarantula.Session;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.marketplace.MarketplaceContext;
import com.tarantula.platform.presence.PresenceContext;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by yinghu lu on 10/19/2017.
 */
public class OnPurchaseCommand implements Callable<Response> {

    private HashMap<String,String> _headers = new HashMap<>();
    private GsonBuilder gsonBuilder;
    private String host;
    private OnAccess access;
    private boolean secured;
    public OnPurchaseCommand(boolean secured,String host, GsonBuilder gsonBuilder, PresenceContext presenceContext,MarketplaceContext marketplaceContext){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        access = new OnAccessTrack();
        //access.accessId(marketplaceContext.marketplaceItemList.get(0).oid());
         presenceContext.lobbyList.forEach((b)->{
             if(b.descriptor().typeId().equals("market")){
                 b.entryList().forEach((d)->{
                     if(d.tag().equals("marketplace")){
                         _headers.put(Session.TARANTULA_MAGIC_KEY,presenceContext.presence.systemId());
                         _headers.put(Session.TARANTULA_TOKEN,presenceContext.presence.token());
                         _headers.put(Session.TARANTULA_APPLICATION_ID,d.applicationId());
                         _headers.put(Session.TARANTULA_TAG,d.tag());
                     }
                 });
             }
         });
    }
    public Response call() throws Exception{
        String ret = new HTTPCaller(secured,host).doAction("onBuy","service/action",_headers,gsonBuilder.create().toJson(access).getBytes());
        return gsonBuilder.create().fromJson(ret,Response.class);
    }
}
