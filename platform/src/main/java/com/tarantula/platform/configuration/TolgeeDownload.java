package com.tarantula.platform.configuration;

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

public class TolgeeDownload {
    private static final String LANGUAGE_CODES = "en-US,fr-FR,de-DE,es-ES,it-IT,ja-JP,ko-KR,pt-BR,fr-CA";
    private static final String REST_URL ="https://loc.e8-dev.api.nicegang.com/v2/projects/34/translations?languages=%LANGUAGE_CODES%&filterKeyName=%SUBJECT_KEY%&filterKeyName=%BODY_KEY%";
    private static final String en = "en-US";
    private TarantulaLogger logger = JDKLogger.getLogger(TolgeeDownload.class);
    private final ServiceContext serviceContext;

    private final String accessKey;
    private final String subjectKey;
    private final String bodyKey;
    private final ConcurrentHashMap<String,String> subLoc = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,String> bodyLoc = new ConcurrentHashMap<>();

    public TolgeeDownload(MailboxCredentialConfiguration mailboxCredentialConfiguration,ServiceContext serviceContext){
        this.serviceContext  = serviceContext;
        JsonObject gridlyConfig = JsonUtil.parse(mailboxCredentialConfiguration.load());
        this.accessKey = gridlyConfig.get("Key").getAsString();
        this.subjectKey = gridlyConfig.get("SubjectKey").getAsString();
        this.bodyKey = gridlyConfig.get("BodyKey").getAsString();
    }

    public boolean download(){
        try{
            String query = REST_URL.replace("%LANGUAGE_CODES%",LANGUAGE_CODES)
                    .replace("%SUBJECT_KEY%", this.subjectKey)
                    .replace("%BODY_KEY%", this.bodyKey);
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query))
                    .timeout(Duration.ofSeconds(AuthObject.TIMEOUT))
                    .header("X-API-Key", accessKey)
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
                logger.warn("Tolgee got ->"+responseData.dataAsString);
                return false;
            }

            var data = JsonUtil.parse(responseData.dataAsString);
            if(data.has("_embedded"))
            {
                var embeddedData = data.getAsJsonObject("_embedded");
                if(embeddedData.has("keys"))
                {
                    var keyData = embeddedData.getAsJsonArray("keys");
                    keyData.forEach(element -> {
                        var keyObject = element.getAsJsonObject();
                        var keyName = keyObject.get("keyName").getAsString();
                        var localizations = keyObject.getAsJsonObject("translations").asMap();

                        localizations.forEach((languageKey, translationObject)-> {
                            var translationText = translationObject.getAsJsonObject()
                                    .get("text").getAsString();
                            if(keyName.equals(subjectKey))
                            {
                                subLoc.put(languageKey, translationText);
                            }
                            else if(keyName.equals(bodyKey))
                            {
                                bodyLoc.put(languageKey, translationText);
                            }
                        });
                    });
                }
            }
            return true;
        }catch (Exception ex){
            logger.error("Tolgee error",ex);
            return false;
        }
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
