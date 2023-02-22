package com.tarantula.platform.service.deployment;

import com.icodesoftware.PostOffice;
import com.icodesoftware.service.EventService;
import com.tarantula.platform.util.Email;

public class PostOfficeSession implements PostOffice {

    private final EventService eventService;

    public PostOfficeSession(EventService eventService){
        this.eventService = eventService;
    }
    public OnTopic onTopic(String topic){
        return new PostOfficeOnTopic(topic,this.eventService);
    }
    public OnSMS onSMS(String phoneNumber){
        return ((data) -> Email.send(phoneNumber,data));
    }
    public OnEmail onEmail(String emailAddress){
        return ((data) -> Email.send(emailAddress,data));
    }

    public OnTag onTag(String tag){
        return new PostOfficeOnTag(tag,this.eventService);
    }
}
