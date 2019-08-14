package com.tarantula.platform;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import com.tarantula.Descriptor;
import com.tarantula.Lobby;

public class DefaultLobby implements Lobby{

	private Descriptor lobby;
	private ConcurrentHashMap<String,Descriptor> applicationIndex = new ConcurrentHashMap<>();

    public DefaultLobby(Descriptor lobby){
        this.lobby = lobby;
    }

    public void addEntry(Descriptor descriptor){
		this.applicationIndex.put(descriptor.distributionKey(),descriptor);
	}
	public  void removeEntry(String applicationId){
		this.applicationIndex.remove(applicationId);
	}
	public List<Descriptor> entryList(){
		ArrayList<Descriptor> dlist = new ArrayList<>();
		this.applicationIndex.forEach((String k,Descriptor d)-> {
			dlist.add(d);
		});
		return dlist;
	}
    public Descriptor descriptor() {
		return this.lobby;
	}

}
