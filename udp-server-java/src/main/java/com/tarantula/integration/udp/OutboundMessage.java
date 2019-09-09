package com.tarantula.integration.udp;

public class OutboundMessage {
    public String clientId;
    public String label;
    public String data;

    public String toString(){
        return clientId+"<<<>>>"+label+"<<<>>>"+data;
    }
}
