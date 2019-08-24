package com.tarantula;

public interface PostOffice{

    OnLabel onLabel();
    OnTag onTag(String tag);
    OnApplication onApplication(String applicationId);

    interface OnLabel{
        void send(String label,byte[] data);
    }
    interface OnTag{
       void send(String distributionKey,Recoverable data);
    }
    interface OnApplication{
        void send(String distributionKey,Recoverable data);
    }
}
