package com.zhoucong.exchange.messaging;

public interface Messaging {

	enum Topic {
		
		/**
         * Topic name: events to trading-engine.
         */
        TRADE(),
		
		/**
         * Topic name: tick to quotation for generate bars.
         */
        TICK();
		
	}
}
