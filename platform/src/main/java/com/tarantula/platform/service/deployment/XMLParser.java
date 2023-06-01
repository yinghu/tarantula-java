package com.tarantula.platform.service.deployment;

import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tarantula.platform.DeploymentDescriptor;


public class XMLParser extends DefaultHandler{

    public ArrayList<LobbyConfiguration> configurations = new ArrayList();
	
	String tblock ="tarantula";
	String value;
	
	LobbyConfiguration configuration;

	DeploymentDescriptor applicationDescriptor;

    public XMLParser(){
	}
	
	public void parse(InputStream xml) throws Exception{
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser p = factory.newSAXParser();
		p.parse(xml,this);
	}
	@Override
	public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
		if(qname.equals("lobby-context")){
			this.tblock = qname;
			this.configuration = new LobbyConfiguration();
		}
		else if(qname.equals("application-list")){
		    this.tblock = qname;
        }
		else if(tblock.equals("application-list")&&qname.equals("application")){
			this.applicationDescriptor = new DeploymentDescriptor();
		}
	}
	@Override
	public void endElement(String uri, String lname, String qname) throws SAXException {
		try{
			if(tblock.equals("lobby-context")&&qname.equals("type-id")){
				this.configuration.descriptor.typeId(value);
			}
			else if(tblock.equals("lobby-context")&&qname.equals("name")){
				this.configuration.descriptor.name(value);
			}
			else if(tblock.equals("lobby-context")&&qname.equals("type")){
				this.configuration.descriptor.type(value);
			}
            else if(tblock.equals("lobby-context")&&qname.equals("category")){
                this.configuration.descriptor.category(value);
            }
            else if(tblock.equals("lobby-context")&&qname.equals("tag")){
                this.configuration.descriptor.tag(value);
            }
            else if(tblock.equals("lobby-context")&&qname.equals("access-mode")){
                this.configuration.descriptor.accessMode(Integer.parseInt(value));
            }
            else if(tblock.equals("lobby-context")&&qname.equals("access-control")){
                this.configuration.descriptor.accessControl(Integer.parseInt(value));
            }
            else if(tblock.equals("lobby-context")&&qname.equals("deploy-priority")){
				this.configuration.descriptor.deployPriority(Integer.parseInt(value));
			}
            else if(tblock.equals("lobby-context")&&qname.equals("deploy-code")){
                this.configuration.descriptor.deployCode(Integer.parseInt(value));
            }
            else if(tblock.equals("lobby-context")&&qname.equals("reset-enabled")){
                this.configuration.descriptor.resetEnabled(Boolean.parseBoolean(value));
            }
			//application-list
            else if(tblock.equals("application-list")&&qname.equals("type-id")){
                this.applicationDescriptor.typeId(value);
            }
			else if(tblock.equals("application-list")&&qname.equals("name")){
				this.applicationDescriptor.name(value);
			}
			else if(tblock.equals("application-list")&&qname.equals("type")){
                this.applicationDescriptor.type(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("category")){
                this.applicationDescriptor.category(value);
            }
            else if (tblock.equals("application-list")&&qname.equals("reset-enabled")){
                this.applicationDescriptor.resetEnabled(Boolean.parseBoolean(value));
            }
            else if (tblock.equals("application-list")&&qname.equals("tournament-enabled")){
                this.applicationDescriptor.tournamentEnabled(Boolean.parseBoolean(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("entry-cost")){
                this.applicationDescriptor.entryCost(Long.parseLong(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("tag")){
                this.applicationDescriptor.tag(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("deploy-priority")){
                this.applicationDescriptor.deployPriority(Integer.parseInt(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("access-mode")){
                this.applicationDescriptor.accessMode(Integer.parseInt(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("access-control")){
                this.applicationDescriptor.accessControl(Integer.parseInt(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("access-rank")){
                this.applicationDescriptor.accessRank(Integer.parseInt(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("module-id")){
                this.applicationDescriptor.moduleId(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("code-base")){
                this.applicationDescriptor.codebase(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("module-name")){
                this.applicationDescriptor.moduleName(value);
            }
			else if(tblock.equals("application-list")&&qname.equals("class-name")){
				this.applicationDescriptor.applicationClassName(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("module-artifact")){
                this.applicationDescriptor.moduleArtifact(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("module-version")){
                this.applicationDescriptor.moduleVersion(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("log-enabled")){
                this.applicationDescriptor.logEnabled(Boolean.parseBoolean(value));
            }
			else if(tblock.equals("application-list")&&qname.equals("application")){
                this.configuration.applications.add(this.applicationDescriptor);
            }
			else if(qname.equals("lobby-context")){
				this.configurations.add(this.configuration);
			}

		}catch(Exception ex){
			ex.printStackTrace();
            throw new RuntimeException(ex);
		}
	}
	@Override 
	public void characters(char[] ch, int start, int length){
        value = new String(ch,start,length);
	}
}
