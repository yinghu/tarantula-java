package com.tarantula.platform.configuration;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class S3ListBucket extends DefaultHandler{

    private String value;
    private boolean onBucket;
    private Callback callback;

    public void request(S3Client s3Client, ServiceContext serviceContext,Callback callback) throws Exception{
        String h = "https://s3."+s3Client.region()+".amazonaws.com";
        AWSSigner signer = s3Client.signer();
        String date = AWSSigner.signingDate();
        String signature = signer.sign("GET",date,"/");
        String token = new StringBuffer("AWS ").append(s3Client.accessKeyId()).append(":").append(signature).toString();
        HttpRequest _request = HttpRequest.newBuilder()
                .uri(URI.create(h))
                .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT))
                .header(HttpCaller.AUTHORIZATION,token)
                .header("Date",date)
                .header(HttpCaller.ACCEPT, HttpCaller.ACCEPT_JSON)
                .GET()
                .build();
        HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
            responseData.dataAsString = _response.body();
            return _response.statusCode();
        });
        if(code != 200) throw new RuntimeException(responseData.dataAsString);
        parse(new ByteArrayInputStream(responseData.dataAsString.getBytes()),callback);
    }
    private void parse(InputStream xml,Callback onBucket)  throws Exception{
        this.callback = onBucket;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser p = factory.newSAXParser();
        p.parse(xml,this);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(!qName.equals("Bucket")) return;
        onBucket = true;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("Bucket")) {
            onBucket = false;
            return;
        }
        if(!onBucket || !qName.equals("Name")) return;
        callback.onBucket(value);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(onBucket) value = new String(ch,start,length);
    }

    public interface Callback{
        void onBucket(String bucket);
    }
}
