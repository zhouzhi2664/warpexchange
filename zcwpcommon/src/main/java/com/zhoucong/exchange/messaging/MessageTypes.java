package com.zhoucong.exchange.messaging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zhoucong.exchange.message.AbstractMessage;

/**
 * Holds message types.
 */
@Component
public class MessageTypes {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	final String messagePackage = AbstractMessage.class.getPackageName();
	
	final Map<String, Class<? extends AbstractMessage>> messageTypes = new HashMap<>();
	
	public AbstractMessage deserialize(String data) {
		//TODO
		
		return null;
	}
	
}
