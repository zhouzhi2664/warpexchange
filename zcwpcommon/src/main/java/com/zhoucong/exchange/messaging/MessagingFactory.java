package com.zhoucong.exchange.messaging;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.zhoucong.exchange.message.AbstractMessage;
import com.zhoucong.exchange.support.LoggerSupport;

import jakarta.annotation.PostConstruct;

/**
 * 接收和发送消息的入口
 */
@Component
public class MessagingFactory extends LoggerSupport{

	@Autowired
	private MessageTypes messageTypes;
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@PostConstruct
	public void init() throws InterruptedException, ExecutionException {
		logger.info("init kafka admin...");
		//TODO
	}

	public <T extends AbstractMessage> MessageProducer<T> createMessageProducer(Messaging.Topic topic,
			Class<T> messageClass){
		logger.info("try create message producer for topic {}...", topic);
		final String name = topic.name();
		return new MessageProducer<>() {
			@Override
			public void sendMessage(AbstractMessage message) {
				kafkaTemplate.send(name, messageTypes.serialize(message));
			}
		};
	}
	
}
