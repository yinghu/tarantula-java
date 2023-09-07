package com.icodesoftware;

import java.util.List;


public interface Lobby {

	Descriptor descriptor();

	List<Descriptor> entryList();

	void addEntry(Descriptor descriptor);
	void removeEntry(long applicationId);

	void addListener(Listener listener);

	interface Listener{
		void onLobby(Descriptor descriptor);
	}
}
