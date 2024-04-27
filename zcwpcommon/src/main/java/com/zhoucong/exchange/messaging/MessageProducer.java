package com.zhoucong.exchange.messaging;

import java.util.List;

import com.zhoucong.exchange.message.AbstractMessage;

@FunctionalInterface
public interface MessageProducer<T extends AbstractMessage> {

	void sendMessage(T message);
	
	default void sendMessages(List<T> messages) {
        for (T message : messages) {
            sendMessage(message);
        }
    }
}
