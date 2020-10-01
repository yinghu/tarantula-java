package com.tarantula;

import com.icodesoftware.Descriptor;

import java.util.List;


public interface Lobby{

	Descriptor descriptor();

	List<Descriptor> entryList();

	void addEntry(Descriptor descriptor);
	void removeEntry(String applicationId);

	void addListener(Listener listener);

	interface Listener{
		void on(Descriptor descriptor);
	}
}
