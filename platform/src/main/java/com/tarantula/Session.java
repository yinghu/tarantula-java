package com.tarantula;

/**
 * Session represents the exchange between client and server via asynchronous way.
 * It is not relative to a physical connection.
 * Updated by yinghu lu on 6/29/2020
 * */

public interface Session extends OnApplication{
    //standard HTTP headers
    String HTTP_CONTENT_TYPE ="Content-type";

    //tarantula headers
    String TARANTULA_APPLICATION_ID ="Tarantula-application-id";
    String TARANTULA_INSTANCE_ID ="Tarantula-instance-id";
    String TARANTULA_TYPE_ID ="Tarantula-type-id";
    String TARANTULA_VIEW_ID ="Tarantula-view-id";
    String TARANTULA_SERVER_ID ="Tarantula-server-id";

    String TARANTULA_TOKEN ="Tarantula-token";
    String TARANTULA_ACCESS_KEY ="Tarantula-access-key";
    String TARANTULA_PAYLOAD_SIZE ="Tarantula-payload-size";
    String TARANTULA_ACTION ="Tarantula-action";
    String TARANTULA_MAGIC_KEY ="Tarantula-magic-key"; //the routing key
    String TARANTULA_TAG ="Tarantula-tag";
    String TARANTULA_NAME ="Tarantula-name";
    //String TARANTULA_PASSWORD ="Tarantula-password";
    String TARANTULA_PAYLOAD = "Tarantula-payload";
    String TARANTULA_ACCESS_MODE = "Tarantula-access-mode";


    int FAST_PLAY_MODE = 2;
    int INVITATION_PLAY_MODE = 4;
    int OFF_LINE_MODE = 3;


    String source();
    void source(String source);


    String sessionId();
    void sessionId(String sessionId);

    boolean joined();
    void joined(boolean joined);

    //write back on http batch
    void write(byte[] delta,int batch,String contentType,String label);
    void write(byte[] delta,int batch,String contentType,String label,boolean closed);

    //write back on label ( ignore on http )
    void write(byte[] payload,String label);
    void write(byte[] payload,String label,boolean closed);

    boolean closed();
    void closed(boolean closed);

    String clientId();
    void clientId(String clientId);

    //boolean forwarding();
    //void forwarding(boolean forwarding);

    boolean streaming();
    void streaming(boolean streaming);

    String action();
    void action(String action);

    String contentType();
    void contentType(String contentType);

    //client payload on wire
    byte[] payload();
    void payload(byte[] data);

    String trackId();
    void trackId(String trackId);

    interface TimeoutListener{
        void onIdle(Session session);
        void onTimeout(Session session);
    }
}
