package com.zhoucong.exchange.messaging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zhoucong.exchange.util.JsonUtil;
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
		int pos = data.indexOf(SEP);
		if (pos == -1) {
            throw new RuntimeException("Unable to handle message with data: " + data);
        }
		String type = data.substring(0, pos);
		Class<? extends AbstractMessage> clazz = messageTypes.get(type);
		if (clazz == null) {
            throw new RuntimeException("Unable to handle message with type: " + type);
        }
		String json = data.substring(pos + 1);
        return JsonUtil.readJson(json, clazz);
	}
	
	private static final char SEP = '#';
}
