package com.zhoucong.exchange.order;


import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.zhoucong.exchange.assets.AssetService;
import com.zhoucong.exchange.enums.AssetEnum;
import com.zhoucong.exchange.enums.Direction;
import com.zhoucong.exchange.model.trade.OrderEntity;

public class OrderService {
	
	final AssetService assetService;
	
	public OrderService(@Autowired AssetService assetService) {
        this.assetService = assetService;
    }
	
	// 跟踪所有活动订单: Order ID => OrderEntity
    final ConcurrentMap<Long, OrderEntity> activeOrders = new ConcurrentHashMap<>();

    // 跟踪用户活动订单: User ID => Map(Order ID => OrderEntity)
    final ConcurrentMap<Long, ConcurrentMap<Long, OrderEntity>> userOrders = new ConcurrentHashMap<>();
	
    public ConcurrentMap<Long, OrderEntity> getActiveOrders() {
        return this.activeOrders;
    }
    
    // 根据订单ID查询Order，不存在返回null:
    public OrderEntity getOrder(Long orderId) {
        return this.activeOrders.get(orderId);
    }
    // 根据用户ID查询用户所有活动Order，不存在返回null:
    public ConcurrentMap<Long, OrderEntity> getUserOrders(Long userId) {
        return this.userOrders.get(userId);
    }

    /**
     * 创建订单，失败返回null:
     */
    public OrderEntity createOrder(long sequenceId, long ts, Long orderId, Long userId, Direction direction, 
    		BigDecimal price, BigDecimal quantity) {
    	switch (direction) {
        case BUY -> {
            // 买入，需冻结USD：
            if (!assetService.tryFreeze(userId, AssetEnum.USD, price.multiply(quantity))) {
                return null;
            }
        }
        case SELL -> {
            // 卖出，需冻结BTC：
            if (!assetService.tryFreeze(userId, AssetEnum.BTC, quantity)) {
                return null;
            }
        }
        default -> throw new IllegalArgumentException("Invalid direction.");
        }
    	// 实例化Order:
        OrderEntity order = new OrderEntity();
        order.id = orderId;
        order.sequenceId = sequenceId;
        order.userId = userId;
        order.direction = direction;
        order.price = price;
        order.quantity = quantity;
        order.unfilledQuantity = quantity;
        order.createdAt = order.updatedAt = ts;
        // 添加到ActiveOrders:
        this.activeOrders.put(order.id, order);
        // 添加到UserOrders:
        ConcurrentMap<Long, OrderEntity> uOrders = this.userOrders.get(userId);
        if (uOrders == null) {
            uOrders = new ConcurrentHashMap<>();
            this.userOrders.put(userId, uOrders);
        }
        uOrders.put(order.id, order);
        return order;
    }
    
    // 删除活动订单:
    public void removeOrder(Long orderId) {
    	// 从ActiveOrders中删除:
    	OrderEntity removed = this.activeOrders.remove(orderId);
        if (removed == null) {
            throw new IllegalArgumentException("Order not found by orderId in active orders: " + orderId);
        }
    	// 从UserOrders中删除:
        ConcurrentMap<Long, OrderEntity> uOrders = userOrders.get(removed.userId);
        if (uOrders == null) {
            throw new IllegalArgumentException("User orders not found by userId: " + removed.userId);
        }
        if (uOrders.remove(orderId) == null) {
            throw new IllegalArgumentException("Order not found by orderId in user orders: " + orderId);
        }
    }
    
}
