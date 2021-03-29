package com.icodesoftware;

public interface PostOffice {

    //app to client
    OnConnection onConnection(Connection connection);
    OnEmail onEmail();
    OnSMS onSMS();

    //app to app
    OnTag onTag(String tag);
    OnTopic onTopic();

    //send data to client on the notification topic
    interface OnTopic{
        void send(String topic,Recoverable data);
    }

    //send data to client on the connection
    interface OnConnection{
        void send(String label,byte[] data);
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
