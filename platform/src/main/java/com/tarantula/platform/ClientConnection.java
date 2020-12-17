package com.tarantula.platform;

import com.icodesoftware.Connection;

import java.util.Map;

/**
 * Created by yinghu lu on 12/17/2020.
 */
public class ClientConnection extends ResponseHeader implements Connection {

    protected String type;
    protected String serverId;
    protected int connectionId;
    protected int sessionId;
    protected int sequence;
    protected boolean secured;
    protected String protocol;
    protected String host;
    protected int port;
    protected String path;
    protected int messageId;
    protected int messageIdOffset;
    protected int maxConnections;
    protected Connection server;

    @Override
    public String type() {
        return type;
    }

    @Override
    public void type(String type) {
        this.type = type;
    }

    @Override
    public String serverId() {
        return serverId;
    }

    @Override
    public void serverId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public int connectionId() {
        return 0;
    }

    @Override
    public void connectionId(int connectionId) {

    }

    @Override
    public int sessionId() {
        return 0;
    }

    @Override
    public void sessionId(int i) {

    }

    @Override
    public int sequence() {
        return 0;
    }

    @Override
    public void sequence(int i) {

    }

    @Override
    public boolean secured() {
        return false;
    }

    @Override
    public void secured(boolean b) {

    }

    @Override
    public String protocol() {
        return null;
    }

    @Override
    public void protocol(String s) {

    }

    @Override
    public String subProtocol() {
        return null;
    }

    @Override
    public void subProtocol(String s) {

    }

    @Override
    public String host() {
        return null;
    }

    @Override
    public void host(String s) {

    }

    @Override
    public int port() {
        return 0;
    }

    @Override
    public void port(int i) {

    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public void path(String s) {

    }

    @Override
    public int messageId() {
        return 0;
    }

    @Override
    public int messageIdOffset() {
        return 0;
    }

    @Override
    public void messageId(int i) {

    }

    @Override
    public void messageIdOffset(int i) {

    }

    @Override
    public int maxConnections() {
        return 0;
    }

    @Override
    public void maxConnections(int i) {

    }

    @Override
    public Connection server() {
        return null;
    }

    @Override
    public void server(Connection connection) {

    }
}
