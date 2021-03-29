package com.tarantula.platform.service.deployment;

import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.icodesoftware.Configuration;
import com.icodesoftware.OnView;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.OnViewTrack;
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
	OnView view;
    String pname;

    Configuration applicationConfiguration;

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
		else if(qname.equals("view-list")){
		    this.tblock = qname;
        }
		else if(qname.equals("view")){
            this.view = new OnViewTrack();
            this.view.viewId(attributes.getValue("type"));
        }
        else if(tblock.equals("view-list")&&qname.equals("property")){
            pname = attributes.getValue("name");
        }
        else if(qname.equals("configuration-list")){
            this.tblock = qname;
        }
        else if(qname.equals("configuration")){
            this.applicationConfiguration = new ApplicationConfiguration();
            //this.applicationConfiguration.tag(attributes.getValue("tag"));
            this.applicationConfiguration.type(attributes.getValue("type"));
        }
        else if(tblock.equals("configuration-list")&&qname.equals("property")){
            pname = attributes.getValue("name");
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
            else if(tblock.equals("lobby-context")&&qname.equals("icon")){
                this.configuration.descriptor.icon(value);
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
			else if(tblock.equals("lobby-context")&&qname.equals("description")){
				this.configuration.descriptor.description(value);
			}
            else if(tblock.equals("lobby-context")&&qname.equals("view-id")){
                this.configuration.descriptor.viewId(value);
            }
            else if(tblock.equals("lobby-context")&&qname.equals("deploy-priority")){
				this.configuration.descriptor.deployPriority(Integer.parseInt(value));
			}
            else if(tblock.equals("lobby-context")&&qname.equals("deploy-code")){
                this.configuration.descriptor.deployCode(Integer.parseInt(value));
            }
            else if(tblock.equals("lobby-context")&&qname.equals("response-label")){
                this.configuration.descriptor.responseLabel(value);
            }
            else if(tblock.equals("lobby-context")&&qname.equals("reset-enabled")){
                this.configuration.descriptor.resetEnabled(Boolean.parseBoolean(value));
            }
			//application-list
            else if(tblock.equals("application-list")&&qname.equals("type-id")){
                this.applicationDescriptor.typeId(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("subtype-id")){
				this.applicationDescriptor.subtypeId(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("view-id")){
                this.applicationDescriptor.viewId(value);
            }
			else if(tblock.equals("application-list")&&qname.equals("name")){
				this.applicationDescriptor.name(value);
			}
            else if(tblock.equals("application-list")&&qname.equals("icon")){
                this.applicationDescriptor.icon(value);
            }
			else if(tblock.equals("application-list")&&qname.equals("type")){
                this.applicationDescriptor.type(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("category")){
                this.applicationDescriptor.category(value);
            }
            else if (tblock.equals("application-list")&&qname.equals("singleton")){
                this.applicationDescriptor.singleton(Boolean.parseBoolean(value));
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
            else if(tblock.equals("application-list")&&qname.equals("response-label")){
                this.applicationDescriptor.responseLabel(value);
            }
            else if(tblock.equals("application-list")&&qname.equals("deploy-priority")){
                this.applicationDescriptor.deployPriority(Integer.parseInt(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("access-mode")){
                this.configuration.descriptor.accessMode(Integer.parseInt(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("access-control")){
                this.applicationDescriptor.accessControl(Integer.parseInt(value));
            }
            else if(tblock.equals("application-list")&&qname.equals("access-rank")){
                this.applicationDescriptor.accessRank(Integer.parseInt(value));
            }
			else if(tblock.equals("application-list")&&qname.equals("description")){
				this.applicationDescriptor.description(value);
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
            else if(tblock.equals("application-list")&&qname.equals("timer-on-module")){
                this.applicationDescriptor.timerOnModule(Long.parseLong(value));
            }
			else if(tblock.equals("application-list")&&qname.equals("capacity")){
                this.applicationDescriptor.capacity(Integer.parseInt(value));
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
            else if(tblock.equals("view-list")&&qname.equals("property")){
                if(pname.equals("moduleContext")){
                    view.moduleContext(value);
                }
                else if(pname.equals("moduleResourceFile")){
                    view.moduleResourceFile(value);
                }
            }
            else if(tblock.equals("configuration-list")&&qname.equals("property")){
                this.applicationConfiguration.configure(pname,value);
            }
			else if(tblock.equals("application-list")&&qname.equals("application")){
                this.configuration.applications.add(this.applicationDescriptor);
            }
            else  if(tblock.equals("view-list")&&qname.equals("view")){
                if(view.moduleContext()==null){
                    int _mix = view.moduleResourceFile().lastIndexOf('/');
                    if(_mix<0){
                        view.moduleContext("root");
                    }else{
                        view.moduleContext(view.moduleResourceFile().substring(0,_mix));
                    }
                }
                this.configuration.views.add(view);
            }
            else if(tblock.equals("configuration-list")&&qname.equals("configuration")){
                this.configuration.configurations.add(applicationConfiguration);
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
