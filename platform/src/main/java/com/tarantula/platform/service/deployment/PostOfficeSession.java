package com.tarantula.platform.service.deployment;

import com.icodesoftware.PostOffice;
import com.icodesoftware.service.EventService;
import com.tarantula.platform.util.Email;

public class PostOfficeSession implements PostOffice {

    private final EventService eventService;

    public PostOfficeSession(EventService eventService){
        this.eventService = eventService;
    }
    public OnTopic onTopic(){
        return new PostOfficeOnTopic(this.eventService);
    }
    public OnSMS onSMS(){
        return ((emailAddress, data) -> Email.send(emailAddress,data));
    }
    public OnEmail onEmail(){
        return ((emailAddress, data) -> Email.send(emailAddress,data));
    }

    public OnTag onTag(String tag){
        return new PostOfficeOnTag(tag,this.eventService);
    }
}
