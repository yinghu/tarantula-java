package com.tarantula.platform.configuration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

public class S3ListBucket extends DefaultHandler{


    private String value;
    public void parse(InputStream xml)  throws Exception{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser p = factory.newSAXParser();
        p.parse(xml,this);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("Name")){
            System.out.println(value);
        }
        //super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        value = new String(ch,start,length);
        //super.characters(ch, start, length);
    }
}
