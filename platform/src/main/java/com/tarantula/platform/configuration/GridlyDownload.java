package com.tarantula.platform.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.inbox.Announcement;
import com.tarantula.platform.service.AuthObject;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;


public class GridlyDownload {

    private static final String REST_URL ="https://api.gridly.com/v1/views/%VIEW_ID%/records";
    private static final String en = "enUS";
    private TarantulaLogger logger = JDKLogger.getLogger(GridlyDownload.class);
    private final MailboxCredentialConfiguration mailboxCredentialConfiguration;
    private final ServiceContext serviceContext;

    private final String accessKey;
    private final String viewId;
    private final String subjectKey;
    private final String bodyKey;
    private final ConcurrentHashMap<String,String> subLoc = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,String> bodyLoc = new ConcurrentHashMap<>();

    public GridlyDownload(MailboxCredentialConfiguration mailboxCredentialConfiguration,ServiceContext serviceContext){
        this.mailboxCredentialConfiguration = mailboxCredentialConfiguration;
        this.serviceContext  = serviceContext;
        JsonObject gridlyConfig = JsonUtil.parse(mailboxCredentialConfiguration.load());
        this.accessKey = gridlyConfig.get("Key").getAsString();
        this.viewId = gridlyConfig.get("ViewId").getAsString();
        this.subjectKey = gridlyConfig.get("SubjectKey").getAsString();
        this.bodyKey = gridlyConfig.get("BodyKey").getAsString();
    }

    public boolean download(){
        try{
            String query = REST_URL.replace("%VIEW_ID%",viewId);
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query))
                    .timeout(Duration.ofSeconds(AuthObject.TIMEOUT))
                    .header(AuthObject.AUTHORIZATION, "ApiKey " +accessKey)
                    .header(AuthObject.ACCEPT, AuthObject.ACCEPT_JSON)
                    .GET()
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = this.serviceContext.httpClientProvider().request(client -> {
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            if(code!=200){
                logger.warn("Gridly say ->"+responseData.dataAsString);
                return false;
            }

            JsonArray data = JsonUtil.parseAsJsonArray(responseData.dataAsString);
            data.forEach(e->{
                JsonObject view = e.getAsJsonObject();
                String id = view.get("id").getAsString();
                if(id.equals(subjectKey)){
                    String defaultHeader = GetDefaultContent(view);
                    view.get("cells").getAsJsonArray().forEach(c->{
                        JsonObject loc = c.getAsJsonObject();
                        subLoc.put(loc.get("columnId").getAsString(),loc.get("value") == null ? defaultHeader : loc.get("value").getAsString());
                    });
                }
                if(id.equals(bodyKey)){
                    String defaultBody = GetDefaultContent(view);
                    view.get("cells").getAsJsonArray().forEach(c->{
                        JsonObject loc = c.getAsJsonObject();
                        bodyLoc.put(loc.get("columnId").getAsString(),loc.get("value") == null ? defaultBody : loc.get("value").getAsString());
                    });
                }

            });
            return true;
        }catch (Exception ex){
            logger.error("Gridly error",ex);
            return false;
        }
    }

    private String GetDefaultContent(JsonObject view) {
        JsonArray cells = view.get("cells").getAsJsonArray();
        for (int i = 0; i < cells.size(); i++) {
            var loc = cells.get(i).getAsJsonObject();
            if (loc.get("columnId").getAsString().equals("enUS")) {
                return loc.get("columnId").getAsString();
            }
        }
        return "";
    }

    public Announcement announcement(String locId){
        String sub = subLoc.get(locId);
        String body = bodyLoc.get(locId);
        if(sub!=null && body!=null) return new Announcement(sub,body);
        String engSub = subLoc.get(en);
        String engBody = bodyLoc.get(en);
        if(engSub!=null && engBody!=null) return new Announcement(engSub,engBody);
        return null;
    }

}
