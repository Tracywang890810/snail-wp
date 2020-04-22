package com.seblong.wp.services;

import java.util.Set;

public interface PushService {

	void deleteTag( String tag );
	
	void addToTag( String tag, Set<String> userIds );
	
	void send(String content, String url, String imageUrl, long sendDate, String group, String messageType);
}
