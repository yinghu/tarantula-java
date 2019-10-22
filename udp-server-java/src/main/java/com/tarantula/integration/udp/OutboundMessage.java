package com.tarantula.integration.udp;


public class OutboundMessage {
    public String clientId;
    public String instanceId;
    public String label;
    public String query;
    public String data;

    public boolean onHeader = true;
    public boolean ended = false;



    public String toString(){
        return clientId+"<<>>"+query+"<<>>"+label+"<<>>"+instanceId+"<<>>"+data;
    }
    interface OnResponse{
        void on(OutboundMessage outboundMessage);
    }
}
