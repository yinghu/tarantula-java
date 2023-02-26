package com.icodesoftware;

public interface PostOffice {

    //app to client
    OnEmail onEmail(String emailAddress);
    OnSMS onSMS(String phoneNumber);

    //app to app
    OnTag onTag(String tag);
    OnTopic onTopic(String topic);

    //send data to client on the notification topic
    interface OnTopic{
        //void send(Recoverable data);
        void send(String trackId,byte[] data);
        void send(String trackId,Session.Header header,byte[] data);
    }

    interface OnEmail{
        boolean send(String text);
    }

    interface OnSMS{
        void send(String text);
    }

    //app to app messaging
    interface OnTag{
       void send(String distributionKey, Recoverable data);
    }

}
