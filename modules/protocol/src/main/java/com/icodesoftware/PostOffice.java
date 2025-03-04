package com.icodesoftware;

import com.icodesoftware.service.HttpClientProvider;

public interface PostOffice extends Closable{

    //app to client
    default OnEmail onEmail(String emailAddress){ return null;}
    default OnSMS onSMS(String phoneNumber){ return null;}

    default OnEvent onEvent(){ return null;}

    default OnHttp onHttp(){ return null;}

    interface OnEmail{
        boolean send(String text);
    }

    interface OnSMS{
        void send(String text);
    }

    interface OnEvent{
        <T extends Event> void send(T event);
    }

    interface OnHttp{
        int request(HttpClientProvider.OnRequest onRequest);
    }

}
