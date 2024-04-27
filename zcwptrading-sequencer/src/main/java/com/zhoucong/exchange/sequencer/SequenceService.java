package com.zhoucong.exchange.sequencer;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Component;

import com.zhoucong.exchange.message.event.AbstractEvent;
import com.zhoucong.exchange.messaging.MessageProducer;
import com.zhoucong.exchange.messaging.MessageTypes;
import com.zhoucong.exchange.messaging.Messaging;
import com.zhoucong.exchange.messaging.MessagingFactory;
import com.zhoucong.exchange.support.LoggerSupport;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Sequence events.
 */
@Component
public class SequenceService extends LoggerSupport {

	private static final String GROUP_ID = "SequencerGroup";
	
	@Autowired
	private SequenceHandler sequenceHandler;
	
	@Autowired
	private MessagingFactory messagingFactory;
	
	@Autowired
	private MessageTypes messageTypes;
	
	private MessageProducer<AbstractEvent> messageProducer;
	
	private AtomicLong sequence;
	private Thread jobThread;
	private boolean running;
	
	private boolean crash = false;
	
	@PostConstruct
	public void init() {
		Thread thread = new Thread(() -> {
			logger.info("start sequence job...");
			// TODO: try get global DB lock:
            // while (!hasLock()) { sleep(10000); }
			this.messageProducer = this.messagingFactory.createMessageProducer(Messaging.Topic.TRADE, 
					AbstractEvent.class);
			//TODO
		});
	}
	
	@PreDestroy
	public void shutdown() {
		logger.info("shutdown sequence service...");
		running = false;
		//TODO
	}
	
	private void sendMessages(List<AbstractEvent> messages) {
		this.messageProducer.sendMessages(messages);
	}
	
	private synchronized void processMessages(List<AbstractEvent> messages) {
		if (!running || crash) {
            panic();
            return;
        }
		if (logger.isInfoEnabled()) {
            logger.info("do sequence for {} messages...", messages.size());
        }
		Long start = System.currentTimeMillis();
		List<AbstractEvent> sequenced = null;
		try {
			sequenced = this.sequenceHandler.sequenceMessages(this.messageTypes, this.sequence, messages);
		} catch (Throwable e) {
			logger.error("exception when do sequence", e);
			shutdown();
            panic();
			throw new Error(e);
		}
		if (logger.isDebugEnabled()) {
			long end = System.currentTimeMillis();
			logger.info("sequenced {} messages in {} ms. current sequence id: {}", messages.size(), (end - start),
					this.sequence.get());
		}
		sendMessages(sequenced);
	}
	
	private void panic() {
		this.crash = true;
		this.running = false;
		System.exit(1);
	}
}
