package com.zhoucong.exchange.main;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zhoucong.exchange.message.ApiResultMessage;
import com.zhoucong.exchange.message.event.AbstractEvent;
import com.zhoucong.exchange.message.event.OrderCancelEvent;
import com.zhoucong.exchange.message.event.OrderRequestEvent;
import com.zhoucong.exchange.message.event.TransferEvent;
import com.zhoucong.exchange.model.trade.OrderEntity;
import com.zhoucong.exchange.bean.OrderBookBean;
import com.zhoucong.exchange.message.NotificationMessage;
import com.zhoucong.exchange.model.trade.MatchDetailEntity;
import com.zhoucong.exchange.model.quotation.TickEntity;
import com.zhoucong.exchange.match.MatchDetailRecord;
import com.zhoucong.exchange.enums.MatchType;
import com.zhoucong.exchange.store.StoreService;
import com.zhoucong.exchange.assets.AssetService;
import com.zhoucong.exchange.clearing.ClearingService;
import com.zhoucong.exchange.match.MatchEngine;
import com.zhoucong.exchange.match.MatchResult;
import com.zhoucong.exchange.order.OrderService;
import com.zhoucong.exchange.support.LoggerSupport;


@Component
public class TradingEngineService extends LoggerSupport{
	
	@Autowired(required = false)
    ZoneId zoneId = ZoneId.systemDefault();
	
	@Value("#{exchangeConfiguration.orderBookDepth}")
    int orderBookDepth = 100;
	
	@Value("#{exchangeConfiguration.debugMode}")
    boolean debugMode = false;
	
	boolean fatalError = false;
	
	@Autowired
    AssetService assetService;

    @Autowired
    OrderService orderService;

    @Autowired
    MatchEngine matchEngine;
    
    @Autowired
    StoreService storeService;

    @Autowired
    ClearingService clearingService;
    
    private long lastSequenceId = 0;
    
    private boolean orderBookChanged = false;
    
    private OrderBookBean latestOrderBook = null;
    private Queue<List<OrderEntity>> orderQueue = new ConcurrentLinkedQueue<>();
    private Queue<List<MatchDetailEntity>> matchQueue = new ConcurrentLinkedQueue<>();
    private Queue<ApiResultMessage> apiResultQueue = new ConcurrentLinkedQueue<>();
    private Queue<NotificationMessage> notificationQueue = new ConcurrentLinkedQueue<>();
    
    
    
    private void runDbThread() {
    	logger.info("start batch insert to db...");
    	for (;;) {
    		try {
                saveToDb();
            } catch (InterruptedException e) {
                logger.warn("{} was interrupted.", Thread.currentThread().getName());
                break;
            }
    	}
    }
    
    // called by dbExecutor thread only:
    private void saveToDb() throws InterruptedException {
    	 if (!matchQueue.isEmpty()) {
    		 List<MatchDetailEntity> batch = new ArrayList<>(1000);
    		 for (;;) {
    			 List<MatchDetailEntity> matches = matchQueue.poll();
    			 if (matches != null) {
                     batch.addAll(matches);
                     if (batch.size() >= 1000) {
                         break;
                     }
                 } else {
                     break;
                 }
    		 }
    		 batch.sort(MatchDetailEntity::compareTo);
    		 if (logger.isDebugEnabled()) {
                 logger.debug("batch insert {} match details...", batch.size());
             }
    		 this.storeService.insertIgnore(batch);
    	 }
    	 if(!orderQueue.isEmpty()) {
    		 List<OrderEntity> batch = new ArrayList<>(1000);
    		 for(;;) {
    			 List<OrderEntity> orders = orderQueue.poll();
                 if (orders != null) {
                     batch.addAll(orders);
                     if (batch.size() >= 1000) {
                         break;
                     }
                 } else {
                     break;
                 }
    		 }
    		 batch.sort(OrderEntity::compareTo);
             if (logger.isDebugEnabled()) {
                 logger.debug("batch insert {} orders...", batch.size());
             }
             this.storeService.insertIgnore(batch);             
    	 }
    	 if (matchQueue.isEmpty()) {
             Thread.sleep(1);
         }
    }
    
    public void processMessages(List<AbstractEvent> messages) {
    	this.orderBookChanged = false;
    	for (AbstractEvent message : messages) {
            processEvent(message);
        }
    	if (this.orderBookChanged) {
            // 获取最新的OrderBook快照:
            this.latestOrderBook = this.matchEngine.getOrderBook(this.orderBookDepth);
        }
    }

	private void processEvent(AbstractEvent event) {		
		if (this.fatalError) {
            return;
        }
		if (event.sequenceId <= this.lastSequenceId) {
            logger.warn("skip duplicate event: {}", event);
            return;
        }
		// 判断是否丢失了消息:
		if (event.previousId > this.lastSequenceId) {
			logger.warn("event lost: expected previous id {} but actual {} for event {}", this.lastSequenceId,
                    event.previousId, event);
			// 从数据库读取丢失的消息:
		    List<AbstractEvent> events = storeService.loadEventsFromDb(this.lastSequenceId);
		    if (events.isEmpty()) {
                logger.error("cannot load lost event from db.");
                panic();
                return;
            }
		    //处理丢失的消息
		    for (AbstractEvent e : events) {
                this.processEvent(e);
            }
            return;
		}
		// 判断当前消息是否指向上一条消息:
		if (event.previousId != lastSequenceId) {
			logger.error("bad event: expected previous id {} but actual {} for event: {}", this.lastSequenceId,
                    event.previousId, event);
            panic();
            return;
		}
		if (logger.isDebugEnabled()) {
            logger.debug("process event {} -> {}: {}...", this.lastSequenceId, event.sequenceId, event);
        }
		try {
            if (event instanceof OrderRequestEvent) {
                createOrder((OrderRequestEvent) event);
            } else if (event instanceof OrderCancelEvent) {
                cancelOrder((OrderCancelEvent) event);
            } else if (event instanceof TransferEvent) {
                transfer((TransferEvent) event);
            } else {
            	logger.error("unable to process event type: {}", event.getClass().getName());
                panic();
                return;
            }
        } catch (Exception e) {
        	logger.error("process event error.", e);
            panic();
            return;
        }
		this.lastSequenceId = event.sequenceId;
		if (logger.isDebugEnabled()) {
            logger.debug("set last processed sequence id: {}...", this.lastSequenceId);
        }
		if (debugMode) {
            this.validate();
            this.debug();
        }
	}
	
	private void panic() {
		logger.error("application panic. exit now...");
        this.fatalError = true;
        System.exit(1);		
	}
	
	boolean transfer(TransferEvent event) {
		//TODO
		return true;
	}

	void createOrder(OrderRequestEvent event) {
		ZonedDateTime zdt = Instant.ofEpochMilli(event.createdAt).atZone(zoneId);
        int year = zdt.getYear();
        int month = zdt.getMonth().getValue();
		// 生成Order ID:
	    long orderId = event.sequenceId * 10000 + (year * 100 + month);
	    // 创建Order:
	    OrderEntity order = this.orderService.createOrder(event.sequenceId, event.createdAt, orderId, event.userId,
	    		event.direction, event.price, event.quantity);
	    if (order == null) {
	        logger.warn("create order failed.");
	        // 推送失败结果:
            this.apiResultQueue.add(ApiResultMessage.createOrderFailed(event.refId, event.createdAt));
	        return;
	    }
	    // 撮合:
	    MatchResult result = matchEngine.processOrder(event.sequenceId, order);
	    // 清算:
	    this.clearingService.clearMatchResult(result);
	    // 推送成功结果,注意必须复制一份OrderEntity,因为将异步序列化:
        this.apiResultQueue.add(ApiResultMessage.orderSuccess(event.refId, order.copy(), event.createdAt));
        this.orderBookChanged = true;
        // 收集Notification:
        List<NotificationMessage> notifications = new ArrayList<>();
        notifications.add(createNotification(event.createdAt, "order_matched", order.userId, order.copy()));
        // 收集已完成的OrderEntity并生成MatchDetailEntity, TickEntity:
        if (!result.matchDetails.isEmpty()) {
        	List<OrderEntity> closedOrders = new ArrayList<>();
            List<MatchDetailEntity> matchDetails = new ArrayList<>();
            List<TickEntity> ticks = new ArrayList<>();
            if (result.takerOrder.status.isFinalStatus) {
                closedOrders.add(result.takerOrder);
            }
            for (MatchDetailRecord detail : result.matchDetails) {
            	OrderEntity maker = detail.makerOrder();
            	notifications.add(createNotification(event.createdAt, "order_matched", maker.userId, maker.copy()));
            	if (maker.status.isFinalStatus) {
                    closedOrders.add(maker);
                }
            	MatchDetailEntity takerDetail = generateMatchDetailEntity(event.sequenceId, event.createdAt, detail,
                        true);
                MatchDetailEntity makerDetail = generateMatchDetailEntity(event.sequenceId, event.createdAt, detail,
                        false);
                matchDetails.add(takerDetail);
                matchDetails.add(makerDetail);
                //TODO               
            }
            // 异步写入数据库:
            this.orderQueue.add(closedOrders);
            this.matchQueue.add(matchDetails);
            //TODO            
            // 异步通知OrderMatch:
            this.notificationQueue.addAll(notifications);
        
        }    
	}
	
	private NotificationMessage createNotification(long ts, String type, Long userId, Object data) {
		NotificationMessage msg = new NotificationMessage();
		msg.createdAt = ts;
        msg.type = type;
        msg.userId = userId;
        msg.data = data;
		return msg;
	}
	
	MatchDetailEntity generateMatchDetailEntity(long sequenceId, long timestamp, MatchDetailRecord detail,
            boolean forTaker) {
		MatchDetailEntity mde = new MatchDetailEntity();
		mde.sequenceId = sequenceId;
		mde.orderId = forTaker ? detail.takerOrder().id : detail.makerOrder().id;
		mde.counterOrderId = forTaker ? detail.makerOrder().id : detail.takerOrder().id;
		mde.direction = forTaker ? detail.takerOrder().direction : detail.makerOrder().direction;
		mde.price = detail.price();
		mde.quantity = detail.quantity();
		mde.type = forTaker ? MatchType.TAKER : MatchType.MAKER;
		mde.userId = forTaker ? detail.takerOrder().userId : detail.makerOrder().userId;
		mde.counterUserId = forTaker ? detail.makerOrder().userId : detail.takerOrder().userId;
		mde.createdAt = timestamp;
		return mde;
	}
	
	void cancelOrder(OrderCancelEvent event) {
		//TODO
	}
	
	public void debug() {
		//TODO
	}
	
	void validate() {
		//TODO
	}
 
}
