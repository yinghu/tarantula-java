package com.tarantula.platform.service.deployment;

import com.tarantula.Configuration;
import com.tarantula.Recoverable;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.CompositeKey;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Updated by yinghu on 6/27/2018.
 */
public class ServiceConfigurationParser extends DefaultHandler {


    public HashMap<String,ServiceConfiguration> configurationMapping = new HashMap<>();

    private String tag;
    private Recoverable.Key configuration;
    private String name;
    private String value;

    public void parse(InputStream xml){
        try{
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser p = factory.newSAXParser();
            p.parse(xml,this);
        }catch (Exception ex){
            throw new RuntimeException("unexpected error on xml parsing",ex);
        }
    }

    @Override
    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        if(qname.equals("configuration")){
            this.tag = attributes.getValue("tag");
            int priority = 0;
            if(attributes.getValue("priority")!=null){
                priority = Integer.parseInt(attributes.getValue("priority"));
            }
            this.configurationMapping.put(this.tag,new ServiceConfiguration(this.tag,priority));
        }
        else if(tag!=null&&qname.equals(this.tag)){
            Configuration tc = new ApplicationConfiguration();
            tc.type(attributes.getValue("type"));
            this.configuration = new CompositeKey(this.tag,tc.type());
            ServiceConfiguration sc = this.configurationMapping.get(this.tag);
            sc.configurationMappings.put(this.configuration,tc);
        }
        else if(tag!=null&&qname.equals("service-provider")){
            ServiceConfiguration sc = this.configurationMapping.get(this.tag);
            sc.serviceProviderName = attributes.getValue("name");
        }
        else if(tag!=null&&qname.equals("property")){
            name = attributes.getValue("name");
        }

    }
    @Override
    public void endElement(String uri, String lname, String qname) throws SAXException {
        if(qname.equals("property")){
            Configuration config = this.configurationMapping.get(this.tag).configurationMappings.get(this.configuration);
            if(config!=null){
                config.configure(name,value);
            }
        }
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.value = (new String(ch,start,length));
    }
}
