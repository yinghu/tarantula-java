package com.tarantula.integration.udp;

public class PendingData {
    public StringBuilder clientId = new StringBuilder();
    public StringBuilder label = new StringBuilder();
    public StringBuilder data = new StringBuilder();
    public boolean onData;
    public boolean onLabel;

    public OutboundMessage reset(){
        OutboundMessage _d = new OutboundMessage();
        _d.clientId = this.clientId.toString();
        _d.label = this.label.toString();
        _d.data = this.data.toString();
        this.clientId.setLength(0);
        this.label.setLength(0);
        this.data.setLength(0);
        this.onData = false;
        this.onLabel = false;
        return _d;
    }
}
