package com.zhoucong.exchange.web.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhoucong.exchange.ApiError;
import com.zhoucong.exchange.ApiErrorResponse;
import com.zhoucong.exchange.ApiException;
import com.zhoucong.exchange.bean.OrderBookBean;
import com.zhoucong.exchange.bean.OrderRequestBean;
import com.zhoucong.exchange.ctx.UserContext;
import com.zhoucong.exchange.message.ApiResultMessage;
import com.zhoucong.exchange.message.event.OrderCancelEvent;
import com.zhoucong.exchange.message.event.OrderRequestEvent;
import com.zhoucong.exchange.redis.RedisCache;
import com.zhoucong.exchange.redis.RedisService;
import com.zhoucong.exchange.service.SendEventService;
import com.zhoucong.exchange.service.TradingEngineApiProxyService;
import com.zhoucong.exchange.support.AbstractApiController;
import com.zhoucong.exchange.util.IdUtil;
import com.zhoucong.exchange.util.JsonUtil;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api")
public class TradingApiController extends AbstractApiController{

	@Autowired
    private SendEventService sendEventService;
	
	@Autowired
    private RedisService redisService;
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
    private TradingEngineApiProxyService tradingEngineApiProxyService;
	
	private Long asyncTimeout = Long.valueOf(500);
	
	private String timeoutJson = null;
	
	Map<String, DeferredResult<ResponseEntity<String>>> deferredResultMap = new ConcurrentHashMap<>();
	
	private String getTimeoutJson() throws IOException {
        if (timeoutJson == null) {
            timeoutJson = this.objectMapper
                    .writeValueAsString(new ApiErrorResponse(ApiError.OPERATION_TIMEOUT, null, ""));
        }
        return timeoutJson;
    }
	
	@PostConstruct
    public void init() {
        this.redisService.subscribe(RedisCache.Topic.TRADING_API_RESULT, this::onApiResultMessage);
    }
	
	@ResponseBody
    @GetMapping(value = "/assets", produces = "application/json")
    public String getAssets() throws IOException {
        return tradingEngineApiProxyService.get("/internal/" + UserContext.getRequiredUserId() + "/assets");
    }
	
	@ResponseBody
    @GetMapping(value = "/orderBook", produces = "application/json")
    public String getOrderBook() {
        String data = redisService.get(RedisCache.Key.ORDER_BOOK);
        return data == null ? OrderBookBean.EMPTY : data;
    }
	
	/**
     * Create a new order.
     */
    @PostMapping(value = "/orders", produces = "application/json")
    @ResponseBody
    public DeferredResult<ResponseEntity<String>> createOrder(@RequestBody OrderRequestBean orderRequest)
            throws IOException {
    	final Long userId = UserContext.getRequiredUserId();
    	orderRequest.validate();
    	final String refId = IdUtil.generateUniqueId();
    	var event = new OrderRequestEvent();
    	event.refId = refId;
        event.userId = userId;
        event.direction = orderRequest.direction;
        event.price = orderRequest.price;
        event.quantity = orderRequest.quantity;
        event.createdAt = System.currentTimeMillis();
        
        ResponseEntity<String> timeout = new ResponseEntity<>(getTimeoutJson(), HttpStatus.BAD_REQUEST);
        DeferredResult<ResponseEntity<String>> deferred = new DeferredResult<>(this.asyncTimeout, timeout);
        deferred.onTimeout(() -> {
            logger.warn("deferred order request refId = {} timeout.", event.refId);
            this.deferredResultMap.remove(event.refId);
        });
        // track deferred:
        this.deferredResultMap.put(event.refId, deferred);
        this.sendEventService.sendMessage(event);
        return deferred;
    }
    
    /**
     * Cancel an order.
     *
     * @param orderId The order id.
     */
    @PostMapping(value = "/orders/{orderId}/cancel", produces = "application/json")
    @ResponseBody
    public DeferredResult<ResponseEntity<String>> cancelOrder(@PathVariable("orderId") Long orderId) throws Exception {
        final Long userId = UserContext.getRequiredUserId();
        String orderStr = tradingEngineApiProxyService.get("/internal/" + userId + "/orders/" + orderId);
        if (orderStr.equals("null")) {
            throw new ApiException(ApiError.ORDER_NOT_FOUND, orderId.toString(), "Active order not found.");
        }
        final String refId = IdUtil.generateUniqueId();
        var message = new OrderCancelEvent();
        message.refId = refId;
        message.refOrderId = orderId;
        message.userId = userId;
        message.createdAt = System.currentTimeMillis();
        ResponseEntity<String> timeout = new ResponseEntity<>(getTimeoutJson(), HttpStatus.BAD_REQUEST);
        DeferredResult<ResponseEntity<String>> deferred = new DeferredResult<>(this.asyncTimeout, timeout);
        deferred.onTimeout(() -> {
            logger.warn("deferred order {} cancel request refId={} timeout.", orderId, refId);
            this.deferredResultMap.remove(refId);
        });
        // track deferred:
        this.deferredResultMap.put(refId, deferred);
        logger.info("cancel order message created: {}", message);
        this.sendEventService.sendMessage(message);
        return deferred;
    }
	
	/*
	 * message callback
	 */
	public void onApiResultMessage(String msg) {
		logger.info("on subscribed message: {}", msg);
		try {
			ApiResultMessage message = objectMapper.readValue(msg, ApiResultMessage.class);
			if (message.refId != null) {
				DeferredResult<ResponseEntity<String>> deferred = this.deferredResultMap.remove(message.refId);
				if (deferred != null) {
					if (message.error != null) {
                        String error = objectMapper.writeValueAsString(message.error);
                        ResponseEntity<String> resp = new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                        deferred.setResult(resp);
                    } else {
                        ResponseEntity<String> resp = new ResponseEntity<>(JsonUtil.writeJson(message.result),
                                HttpStatus.OK);
                        deferred.setResult(resp);
                    }
				}
			}
		} catch (Exception e) {
            logger.error("Invalid ApiResultMessage: " + msg, e);
        }
	}
}
