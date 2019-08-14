package com.tarantula.platform.service.deployment;

import com.tarantula.RecoverableListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Updated by yinghu on 6/27/2018.
 */
public class PortableProviderConfigurationParser extends DefaultHandler{


    private List<RecoverableListener> configurationMapping = new ArrayList<>();

    private String _config;
    private int oid;
    private String className;
    public PortableProviderConfigurationParser(String config){
        this._config = config;

    }

    public List<RecoverableListener> parse() throws Exception{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser p = factory.newSAXParser();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(_config);
        p.parse(in,this);
        in.close();
        return configurationMapping;
    }

    @Override
    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        if(qname.equals("portable-registry")){
            oid = Integer.parseInt(attributes.getValue("registry-id"));
        }
    }
    @Override
    public void endElement(String uri, String lname, String qname) throws SAXException {
        if(qname.equals("portable-registry")){
            try{
                RecoverableListener rr = (RecoverableListener) Class.forName(className).getConstructor().newInstance();
                if(oid==rr.registryId()){
                    configurationMapping.add(rr);
                }
                else{
                    throw new RuntimeException("wrong registry id ["+oid+"]");
                }
            }catch (Exception ex){
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        className = new String(ch,start,length);
    }

}
