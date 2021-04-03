package com.icodesoftware;


public interface Session extends OnApplication {
    //standard HTTP headers
    String HTTP_CONTENT_TYPE ="Content-type";

    //tarantula headers

    String TARANTULA_TOURNAMENT_ID ="Tarantula-tournament-id";
    String TARANTULA_TYPE_ID ="Tarantula-type-id";
    String TARANTULA_VIEW_ID ="Tarantula-view-id";

    String TARANTULA_SERVER_ID ="Tarantula-server-id";
    String TARANTULA_CONNECTION_ID ="Tarantula-connection-id";
    String TARANTULA_ZONE_ID ="Tarantula-zone-id";
    String TARANTULA_ROOM_ID ="Tarantula-room-id";

    String TARANTULA_TOKEN ="Tarantula-token";
    String TARANTULA_ACCESS_KEY ="Tarantula-access-key";
    String TARANTULA_PAYLOAD_SIZE ="Tarantula-payload-size";
    String TARANTULA_ACTION ="Tarantula-action";
    String TARANTULA_MAGIC_KEY ="Tarantula-magic-key"; //the routing key
    String TARANTULA_TAG ="Tarantula-tag";
    String TARANTULA_NAME ="Tarantula-name";

    String TARANTULA_PAYLOAD = "Tarantula-payload";
    String TARANTULA_ACCESS_MODE = "Tarantula-access-mode";


    int FAST_PLAY_MODE = 2;
    int INVITATION_PLAY_MODE = 4;
    int OFF_LINE_MODE = 3;
    int GAME_CENTER_PLAY_MODE = 5;

    String source();
    void source(String source);


    String sessionId();
    void sessionId(String sessionId);

    boolean joined();
    void joined(boolean joined);

    //write back on http batch
    //void write(byte[] delta,int batch,String contentType);
    //void write(byte[] delta,int batch,String contentType,boolean closed);

    //write back on label ( ignore on http )
    void write(byte[] payload);
    void write(byte[] payload,boolean closed);

    boolean closed();
    void closed(boolean closed);

    String clientId();
    void clientId(String clientId);

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

}
