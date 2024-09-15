package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.inbox.Announcement;
import com.tarantula.platform.item.ConfigurableObject;

import java.time.LocalDateTime;


public class MailboxCredentialConfiguration extends CredentialConfiguration {

    private ServiceContext serviceContext;

    private GridlyDownload gridlyDownload;
    private Content content;

    public MailboxCredentialConfiguration(String typeId, JsonObject configurableObject){
        super(typeId,configurableObject);
        this.name = this.configurationName;
    }

    public MailboxCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,configurableObject.configurationName(),configurableObject);
        this.typeId = typeId;
    }

    @Override
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        this.serviceContext = serviceContext;
        content = serviceContext.deploymentServiceProvider().resource(header.get("GridlyView").getAsString());
        if(!content.existed()) return false;
        gridlyDownload = new GridlyDownload(this,serviceContext);
        return gridlyDownload.download();
    }

    public byte[] load(){
        if(content!=null && content.existed()) return content.data();
        content = serviceContext.deploymentServiceProvider().resource(header.get("GridlyView").getAsString());
        if(!content.existed()) throw new RuntimeException("cannot load gridly view config");
        return content.data();
    }

    public Announcement announcement(String locId){
        return gridlyDownload.announcement(locId);
    }

    public boolean inbox(){
        return header.get("Inbox").getAsBoolean();
    }

    public LocalDateTime startTime() {
        return TimeUtil.fromString("yyyy-MM-dd'T'HH:mm",header.get("StartTime").getAsString());
    }
    public LocalDateTime expirationTime() {
        return TimeUtil.fromString("yyyy-MM-dd'T'HH:mm",header.get("ExpirationTime").getAsString());
    }


    @Override
    public boolean setup(ServiceContext serviceContext) {
        content = serviceContext.node().homingAgent().onDownload(header.get("GridlyView").getAsString());
        if(!content.existed()) return false;
        gridlyDownload = new GridlyDownload(this,serviceContext);
        return gridlyDownload.download();
    }
}
