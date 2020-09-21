package com.tarantula.cci.webhook;

import com.tarantula.Event;
import com.tarantula.cci.OnExchange;
import com.tarantula.platform.util.HttpCaller;

/**
 * Created by yinghu lu on 9/19/2020.
 */
public class WebhookSession implements OnExchange {

    private HttpCaller httpCaller;

    @Override
    public String id() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public String method() {
        return null;
    }

    @Override
    public String header(String name) {
        return null;
    }

    @Override
    public byte[] payload() {
        return new byte[0];
    }

    @Override
    public boolean onEvent(Event event) {
        //use http caller to request the remote game server.
        if(event.payload()!=null&&event.payload().length>0){
            //do post
            //httpCaller.post(event.)
        }
        else{
            //do get
        }
        return false;
    }
}
