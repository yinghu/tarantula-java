package com.tarantula;

public interface PostOffice{

    OnTopic onTopic();
    OnConnection onConnection(String serverId);
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

    
    interface OnTag{
       void send(String distributionKey,Recoverable data);
    }
    interface OnApplication{
        void send(String distributionKey,Recoverable data);
    }
}
