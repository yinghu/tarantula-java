package com.icodesoftware.integration.udp;

import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;

public class ActiveSession extends RecoverableObject implements Session {


    public ActiveSession(String systemId,long stub){
        this.owner = systemId;
        //this.routingNumber = stub;
    }
    @Override
    public String systemId() {
        return owner;
    }

    @Override
    public void systemId(String systemId) {

    }

    @Override
    public long stub() {
        return routingNumber;
    }

    @Override
    public void stub(long stub) {

    }

    @Override
    public String tournamentId() {
        return null;
    }

    @Override
    public void tournamentId(String tournamentId) {

    }

    @Override
    public String typeId() {
        return null;
    }

    @Override
    public void typeId(String typeId) {

    }

    @Override
    public String ticket() {
        return null;
    }

    @Override
    public void ticket(String ticket) {

    }

    @Override
    public String command() {
        return null;
    }

    @Override
    public void command(String command) {

    }

    @Override
    public int code() {
        return 0;
    }

    @Override
    public void code(int code) {

    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public void message(String message) {

    }

    @Override
    public boolean successful() {
        return false;
    }

    @Override
    public void successful(boolean successful) {

    }

    @Override
    public String source() {
        return null;
    }

    @Override
    public void source(String source) {

    }

    @Override
    public String sessionId() {
        return null;
    }

    @Override
    public void sessionId(String sessionId) {

    }

    @Override
    public boolean joined() {
        return false;
    }

    @Override
    public void joined(boolean joined) {

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
        return false;
    }

    @Override
    public void closed(boolean closed) {

    }

    @Override
    public String clientId() {
        return null;
    }

    @Override
    public void clientId(String clientId) {

    }

    @Override
    public String action() {
        return null;
    }

    @Override
    public void action(String action) {

    }

    @Override
    public String contentType() {
        return null;
    }

    @Override
    public void contentType(String contentType) {

    }

    @Override
    public byte[] payload() {
        return new byte[0];
    }

    @Override
    public void payload(byte[] data) {

    }

    @Override
    public String trackId() {
        return null;
    }

    @Override
    public void trackId(String trackId) {

    }

    @Override
    public String token() {
        return null;
    }

    @Override
    public void token(String token) {

    }
}
