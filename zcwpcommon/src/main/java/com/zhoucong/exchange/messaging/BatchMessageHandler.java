package com.zhoucong.exchange.messaging;

import java.util.List;

import com.zhoucong.exchange.message.AbstractMessage;

@FunctionalInterface
public interface BatchMessageHandler<T extends AbstractMessage> {

	void processMessage(List<T> messages);
	
}
