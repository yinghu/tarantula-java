package com.tarantula.platform;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tarantula.Descriptor;
import com.tarantula.Lobby;

public class DefaultLobby implements Lobby{

	private Descriptor lobby;
	private ConcurrentHashMap<String,Descriptor> applicationIndex = new ConcurrentHashMap<>();
	private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    public DefaultLobby(Descriptor lobby){
        this.lobby = lobby;
    }

    public void addEntry(Descriptor descriptor){
		this.applicationIndex.put(descriptor.distributionKey(),descriptor);
		listeners.forEach((l)->{
			l.on(descriptor);
		});
	}
	public  void removeEntry(String applicationId){
		Descriptor removed = this.applicationIndex.remove(applicationId);
		removed.disabled(true);
		listeners.forEach((l)->{
			l.on(removed);
		});
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
	public void addListener(Listener listener){
		this.listeners.add(listener);
	}
}
