package com.icodesoftware.service;

import java.net.http.HttpClient;

public interface HttpClientProvider extends ServiceProvider{

    String NAME = "HttpClientProvider";

    String post(String host,String path,String[] headers,byte[] payload) throws Exception;
    String get(String host,String path,String[] headers) throws Exception;
    int request(OnRequest request) throws Exception;
    void requestAsync(OnRequestAsync requestAsync);

    interface OnRequest{
        int onClient(HttpClient httpClient) throws Exception;
    }

    interface OnRequestAsync{
        void onClient(HttpClient httpClient);
    }
}
