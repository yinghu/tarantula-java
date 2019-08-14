package com.tarantula.platform.service.deployment;

import com.tarantula.ServiceProvider;
import com.tarantula.Serviceable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu on 6/27/2018.
 */
public class ServiceProviderConfigurationParser extends DefaultHandler implements Serviceable{


    private ArrayList<ServiceProviderConfiguration> configurationMapping = new ArrayList<>();

    private String _config;
    private ConcurrentHashMap<String,ServiceProvider> _loaded;
    public ServiceProviderConfigurationParser(String config,ConcurrentHashMap<String,ServiceProvider> _providers){
        this._config = config;
        this._loaded = _providers;
    }

    private void parse(InputStream xml) throws Exception{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser p = factory.newSAXParser();
        p.parse(xml,this);
    }

    @Override
    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        if(qname.equals("service-provider")){
            ServiceProviderConfiguration spc = new ServiceProviderConfiguration();
            spc.serviceProviderImplementation = attributes.getValue("name");
            this.configurationMapping.add(spc);
        }
    }
    @Override
    public void endElement(String uri, String lname, String qname) throws SAXException {
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void start() throws Exception {
        parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(_config));
        configurationMapping.forEach((sp)->{
            ServiceProvider s = newServiceProvider(sp.serviceProviderImplementation);
            _loaded.put(s.name(),s);
        });
    }

    @Override
    public void shutdown() throws Exception {

    }
    private ServiceProvider newServiceProvider(String cname){
        try {
            ServiceProvider sp = (ServiceProvider) Class.forName(cname).getConstructor().newInstance();
            sp.start();
            return sp;
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
