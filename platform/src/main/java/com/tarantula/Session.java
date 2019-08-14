package com.tarantula;

/**
 * Session represents the exchange between client and server via asynchronous way.
 * It is not relative to a physical connection.
 * Updated by yinghu lu on 7/30/2019
 * */

public interface Session extends OnApplication{
    //standard HTTP headers
    String HTTP_CONTENT_TYPE ="Content-type";

    //tarantula headers
    String TARANTULA_APPLICATION_ID ="Tarantula-application-id";
    String TARANTULA_INSTANCE_ID ="Tarantula-instance-id";
    String TARANTULA_VIEW_ID ="View-id";
    String TARANTULA_TOKEN ="Tarantula-token";
    String TARANTULA_PAYLOAD_SIZE ="Tarantula-payload-size";
    String TARANTULA_ACTION ="Tarantula-action";

    String TARANTULA_MAGIC_KEY ="Tarantula-magic-key"; //the routing key

    String TARANTULA_TAG ="Tarantula-tag";

    String X_REAL_IP = "X-real-ip";
    String TARANTULA_PAYLOAD = "Tarantula-payload";


    int FAST_PLAY_MODE = 2;

    int INSTANCE_PLAY_MODE = 4;

    int PUBLIC_ACCESS_MODE = 10;
    int PROTECT_ACCESS_MODE = 11;
    int FORWARD_ACCESS_MODE = 12;

    int PRIVATE_ACCESS_MODE = 13;

    String source();
    void source(String source);


    String sessionId();
    void sessionId(String sessionId);

    boolean joined();
    void joined(boolean joined);

    String ticket();
    void ticket(String ticket);

    //write back on http batch
    void write(byte[] delta,int batch,String contentType,String label);
    void write(byte[] delta,int batch,String contentType,String label,boolean closed);

    //write back on label ( ignore on http )
    void write(byte[] payload,String label);
    void write(byte[] payload,String label,boolean closed);


    String clientId();
    void clientId(String clientId);

    boolean forwarding();
    void forwarding(boolean forwarding);

    boolean streaming();
    void streaming(boolean streaming);

    String action();
    void action(String action);

    //client payload on wire
    byte[] payload();
    void payload(byte[] data);
    String trackId();
    void trackId(String trackId);

}
