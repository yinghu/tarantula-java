package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.Session;
import com.tarantula.logging.JDKLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

/**
 * Created by yinghu lu on 6/22/2018.
 */
public class HttpRepositoryHandler implements HttpHandler {

    private static final JDKLogger log = JDKLogger.getLogger(HttpEndpoint.class);


    private static String m_dir;
    private static String b_dir;
    static {
        m_dir = Paths.get( System.getProperty("user.home")+ FileSystems.getDefault().getSeparator()+".m2/repository/com/tarantula/tarantula-platform/2.0").toString();
        b_dir = Paths.get( System.getProperty("user.home")+ FileSystems.getDefault().getSeparator()+".m2/repository/com/tarantula/tarantula-boost/1.0").toString();

    }
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try{
            String path = httpExchange.getRequestURI().getPath();
            log.warn(path);
            if(path.endsWith("pom")){
                InputStream in = new FileInputStream(m_dir+"/tarantula-platform-2.0.pom");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/xml");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else if(path.endsWith("jar")){
                InputStream in = new FileInputStream(m_dir+"/tarantula-platform-2.0.jar");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"application/java-archive");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else  if(path.endsWith("jar.sha1")){
                InputStream in = new FileInputStream(m_dir+"/tarantula-platform-2.0.jar.sha1");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/text");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else  if(path.endsWith("jar.md5")){
                InputStream in = new FileInputStream(m_dir+"/tarantula-platform-2.0.jar.md5");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/text");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else  if(path.endsWith("pom.sha1")){
                InputStream in = new FileInputStream(m_dir+"/tarantula-platform-2.0.pom.sha1");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/text");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else  if(path.endsWith("pom.md5")){
                InputStream in = new FileInputStream(m_dir+"/tarantula-platform-2.0.pom.md5");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/text");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else if(path.endsWith("sample.zip")){
                InputStream in = new FileInputStream(b_dir+"/tarantula-boost-1.0-bin.zip");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"application/octet-stream");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else if(path.endsWith("sample.tar.gz")){
                InputStream in = new FileInputStream(b_dir+"/tarantula-boost-1.0-bin.tar.gz");
                byte[] ret = new byte[in.available()];
                in.read(ret);
                in.close();
                httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"application/octet-stream");
                httpExchange.sendResponseHeaders(200,ret.length);
                httpExchange.getResponseBody().write(ret);
                httpExchange.close();
            }
            else{
                throw new IOException("bad request");
            }
        }catch (Exception ex){
            throw new IOException("bad request",ex);
        }
    }
}
