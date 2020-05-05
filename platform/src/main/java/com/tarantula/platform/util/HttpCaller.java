package com.tarantula.platform.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpCaller {


    public static void main(String[] args) throws Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://10.0.0.192:8090/user/index"))
            .timeout(Duration.ofSeconds(5))
            .header("Accept", "application/json")
            .header("Tarantula-tag","index/lobby")
            .header("Tarantula-type-id","game-lobby")
            .GET()
            .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}
