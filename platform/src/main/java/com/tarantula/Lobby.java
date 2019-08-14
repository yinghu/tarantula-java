package com.tarantula;

import java.util.List;


public interface Lobby{

	Descriptor descriptor();

	List<Descriptor> entryList();

	void addEntry(Descriptor descriptor);
	void removeEntry(String applicationId);

	interface Listener{
		void on(Descriptor descriptor);
	}
}
