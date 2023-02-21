package com.icodesoftware;

public interface PostOffice {

    //app to client
    OnEmail onEmail();
    OnSMS onSMS();

    //app to app
    OnTag onTag(String tag);
    OnTopic onTopic();

    //send data to client on the notification topic
    interface OnTopic{
        void send(String topic,Recoverable data);
    }

    interface OnEmail{
        boolean send(String emailAddress,String text);
    }

    interface OnSMS{
        void send(String phoneNumber,String text);
    }

    //app to app messaging
    interface OnTag{
       void send(String distributionKey, Recoverable data);
    }

}
