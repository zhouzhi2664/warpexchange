package com.zhoucong.exchange.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zhoucong.exchange.message.event.AbstractEvent;
import com.zhoucong.exchange.messaging.MessageProducer;
import com.zhoucong.exchange.messaging.Messaging;
import com.zhoucong.exchange.messaging.MessagingFactory;

import jakarta.annotation.PostConstruct;

@Component
public class SendEventService {

	@Autowired
    private MessagingFactory messagingFactory;

    private MessageProducer<AbstractEvent> messageProducer;
    
    @PostConstruct
    public void init() {
        this.messageProducer = messagingFactory.createMessageProducer(Messaging.Topic.SEQUENCE, AbstractEvent.class);
    }
    
    public void sendMessage(AbstractEvent message) {
        this.messageProducer.sendMessage(message);
    }
}
