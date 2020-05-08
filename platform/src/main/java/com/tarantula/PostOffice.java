package com.tarantula;

public interface PostOffice{

    //app to client
    OnTopic onTopic();
    OnConnection onConnection(String serverId);
    //app to app
    OnTag onTag(String tag);
    OnApplication onApplication(String applicationId);

    //send data to client on the notification topic
    interface OnTopic{
        void send(String topic,byte[] data);
    }

    //send data to client on the connection
    interface OnConnection{
        void send(String label,byte[] data);
    }

    //app to app messaging
    interface OnTag{
       void send(String distributionKey,Recoverable data);
    }
    interface OnApplication{
        void send(String distributionKey,Recoverable data);
    }
}
