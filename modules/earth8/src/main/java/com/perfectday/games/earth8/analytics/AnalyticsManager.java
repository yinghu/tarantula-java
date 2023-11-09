package com.perfectday.games.earth8.analytics;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.HttpClientProvider;
import com.icodesoftware.util.HttpCaller;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AnalyticsManager {
    private String _endpoint;
    private HttpCaller _provider;
    private TarantulaLogger logger = JDKLogger.getLogger(AnalyticsManager.class);

    public AnalyticsManager(String endpoint) {
        _endpoint = endpoint;
        _provider = new HttpCaller();
        try {
            _provider._init();
        } catch (Exception e) {
            logger.warn(e.toString());
        }
    }

    public void send(AnalyticsTransaction transaction)
    {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(_endpoint))
                .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT))
                .header(HttpCaller.CONTENT_TYPE, HttpCaller.ACCEPT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(transaction.toString()))
                .build();

        try {
            int code = _provider.request(client->{
                HttpResponse<String> _response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return _response.statusCode();
            });
        } catch (Exception e) {
            logger.warn(e.toString());
        }
    }
}
