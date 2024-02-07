package com.zhoucong.exchange.model.trade;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zhoucong.exchange.enums.Direction;
import com.zhoucong.exchange.enums.OrderStatus;
import com.zhoucong.exchange.model.support.EntitySupport;

/**
 * Order entity.
 */
@Entity
@Table(name = "orders")
public class OrderEntity implements EntitySupport, Comparable<OrderEntity>{

	// 订单ID / 定序ID / 用户ID:	
	/**
     * Primary key: assigned order id.
     */
    @Id
    @Column(nullable = false, updatable = false)
	public Long id;    
    /**
     * event id (a.k.a sequenceId) that create this order. ASC only.
     */
    @Column(nullable = false, updatable = false)
    public long sequenceId;    
    /**
     * User id of this order.
     */
    @Column(nullable = false, updatable = false)
    public Long userId;

    // 价格 / 方向 / 状态:
    /**
     * The limit-order price. MUST NOT change after insert.
     */
    @Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)    
    public BigDecimal price;    
    /**
     * Order direction.
     */
    @Column(nullable = false, updatable = false, length = VAR_ENUM)
    public Direction direction;
    
    /**
     * Order status.
     */
    @Column(nullable = false, updatable = false, length = VAR_ENUM)
    public OrderStatus status;
    
    // 订单数量 / 未成交数量:
    /**
     * The order quantity. MUST NOT change after insert.
     */
    @Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
    public BigDecimal quantity;
    /**
     * How much unfilled during match.
     */
    @Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
    public BigDecimal unfilledQuantity;

    // 创建和更新时间:
    /**
     * Created time (milliseconds).
     */
    @Column(nullable = false, updatable = false)
    public long createdAt;
    /**
     * Updated time (milliseconds).
     */
    @Column(nullable = false, updatable = false)
    public long updatedAt;
	
    private int version;
    
    public void updateOrder(BigDecimal unfilledQuantity, OrderStatus status, long updatedAt) {
        this.version++;
        this.unfilledQuantity = unfilledQuantity;
        this.status = status;
        this.updatedAt = updatedAt;
        this.version++;
    }
    
    @Transient
    @JsonIgnore
    public int getVersion() {
        return this.version;
    }
    
    /**
     * 按OrderID排序
     */
    @Override
	public int compareTo(OrderEntity o) {
    	return Long.compare(this.id.longValue(), o.id.longValue());
	}    
}