package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.inbox.Announcement;



public class GridlyDownload {

    private TarantulaLogger logger = JDKLogger.getLogger(GridlyDownload.class);
    private final MailboxCredentialConfiguration mailboxCredentialConfiguration;
    private final ServiceContext serviceContext;
    private final JsonObject gridlyConfig;
    private JsonObject gridlyView;

    public GridlyDownload(MailboxCredentialConfiguration mailboxCredentialConfiguration,ServiceContext serviceContext){
        this.mailboxCredentialConfiguration = mailboxCredentialConfiguration;
        this.serviceContext  = serviceContext;
        this.gridlyConfig = JsonUtil.parse(mailboxCredentialConfiguration.load());
    }

    public boolean download(){
        //call grid using the conf and save gridly loc json onto
        //serviceContext.node().deployDirectory()+"/web/"+mailboxCredentialConfiguration.typeId+"-"+mailboxCredentialConfiguration.distributionId()+".json";
        //cache the loc content into gridlyView
        return true;
    }

    public Announcement announcement(String locId){
        //use locId to get announcement data from gridly view
        return new Announcement();
    }

}
