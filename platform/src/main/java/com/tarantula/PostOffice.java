package com.tarantula;

public interface PostOffice{

    OnTopic onTopic();
    OnConnection onConnection(String serverId);
    OnTag onTag(String tag);
    OnApplication onApplication(String applicationId);

    interface OnTopic{
        void send(String topic,byte[] data);
    }
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
