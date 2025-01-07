package com.icodesoftware.util;

import com.icodesoftware.OnAccess;


public class OnApplicationHeader extends ResponseHeader implements OnAccess {

    protected long tournamentId;

    protected long systemId;

    protected transient String typeId;

    protected transient long stub;

    protected transient String ticket;

    protected transient String token;

    protected transient String source;

    protected transient long sessionId;

    protected transient String clientId;

    protected transient String action;

    protected transient String trackId;

    protected transient String contentType ="application/json";

    protected transient byte[] payload;
    protected transient DataBuffer dataBuffer;

    protected boolean joined;
    protected boolean closed;


    public long systemId() {
        return systemId;
    }

    public void systemId(long systemId) {
        this.systemId = systemId;
    }

    public long tournamentId() {
        return this.tournamentId;
    }

    public void tournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String typeId(){
        return this.typeId;
    }

    public void typeId(String typeId){
        this.typeId = typeId;
    }

    public long stub(){return this.stub;}

    public void stub(long stub){ this.stub = stub;}

    public String ticket(){
        return this.ticket;
    }

    public void ticket(String ticket){
        this.ticket = ticket;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public void source(String source) {
        this.source = source;
    }

    @Override
    public long sessionId() {
        return sessionId;
    }

    @Override
    public void sessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean joined() {
        return joined;
    }

    @Override
    public void joined(boolean joined) {
        this.joined = joined;
    }

    @Override
    public void write(byte[] payload) {

    }

    @Override
    public void write(byte[] payload, boolean closed) {

    }

    @Override
    public void write(Header messageHeader, byte[] payload) {

    }

    @Override
    public boolean closed() {
        return closed;
    }

    @Override
    public void closed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public String clientId() {
        return clientId;
    }

    @Override
    public void clientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public void action(String action) {
        this.action = action;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public void contentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public byte[] payload() {
        return payload;
    }

    @Override
    public void payload(byte[] data) {
        this.payload = data;
    }

    @Override
    public String trackId() {
        return trackId;
    }

    @Override
    public void trackId(String trackId) {
        this.trackId = trackId;
    }

    @Override
    public String token() {
        return token;
    }

    @Override
    public void token(String token) {
        this.token = token;
    }

    public DataBuffer dataBuffer(){
        return dataBuffer;
    }
    public void dataBuffer(DataBuffer dataBuffer){
        this.dataBuffer = dataBuffer;
    }
}
