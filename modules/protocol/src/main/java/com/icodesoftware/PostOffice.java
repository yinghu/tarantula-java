package com.icodesoftware;

import com.icodesoftware.service.HttpClientProvider;

import java.nio.file.Path;

public interface PostOffice extends Closable,Resettable{

    //app to client
    default OnEmail onEmail(String emailAddress){ return null;}
    default OnSMS onSMS(String phoneNumber){ return null;}

    default OnEvent onEvent(){ return null;}

    default OnHttp onHttp(){ return null;}

    default OnFile onFile(){ return null;}

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

    interface OnFile{
        Path home(Session session);
    }

}
