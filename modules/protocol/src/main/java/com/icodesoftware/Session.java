package com.icodesoftware;


public interface Session extends OnApplication {

    String DataStore = "game_session";
    //standard HTTP headers
    String HTTP_CONTENT_TYPE ="Content-type";

    //tarantula headers
    String TARANTULA_TOURNAMENT_ID ="Tarantula-tournament-id";
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
    String TARANTULA_CLIENT_ID ="Tarantula-client-id";

    String TARANTULA_PAYLOAD = "Tarantula-payload";

    String TARANTULA_TRACK_ID ="Tarantula-track-id";

    String TARANTULA_DATA_ENCRYPTED ="Tarantula-data-encrypted";


    String source();
    void source(String source);


    long sessionId();
    void sessionId(long sessionId);

    boolean joined();
    void joined(boolean joined);

    //write back on label ( ignore on http )
    void write(byte[] payload);
    void write(byte[] payload,boolean closed);
    void write(Session.Header messageHeader,byte[] payload);

    boolean closed();
    void closed(boolean closed);

    String clientId();
    void clientId(String clientId);


    String action();
    void action(String action);

    String contentType();
    void contentType(String contentType);

    //client payload on wire
    byte[] payload();
    void payload(byte[] data);

    default DataBuffer dataBuffer(){ return null;}
    default void dataBuffer(DataBuffer dataBuffer){}

    String trackId();
    void trackId(String trackId);

    String token();
    void token(String token);

    interface TimeoutListener{
        void timeout(long systemId,long stub);
    }

    interface Header{
        boolean ack();
        int channelId();
        int sessionId();
        int objectId();
        int sequence();
        short commandId();
        short batchSize();
        short batch();
        boolean broadcasting();
        boolean encrypted();
    }

}
