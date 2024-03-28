package com.zhoucong.exchange.messaging;

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
	
}
