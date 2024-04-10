package com.zhoucong.exchange.model.trade;

import java.math.BigDecimal;

import org.springframework.lang.Nullable;

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
    
    /**
     * 用于推送时的异步序列化
     */
    @Nullable
    public OrderEntity copy() {
    	OrderEntity entity = new OrderEntity();
    	int ver = this.version;
    	if (ver != this.version) {
            return null;
        }
    	entity.status = this.status;
        entity.unfilledQuantity = this.unfilledQuantity;
        entity.updatedAt = this.updatedAt;
        entity.createdAt = this.createdAt;
        entity.direction = this.direction;
        entity.id = this.id;
        entity.price = this.price;
        entity.quantity = this.quantity;
        entity.sequenceId = this.sequenceId;
        entity.userId = this.userId;
    	return entity;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof OrderEntity) {
            OrderEntity e = (OrderEntity) o;
            return this.id.longValue() == e.id.longValue();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    @Transient
    @JsonIgnore
    public int getVersion() {
        return this.version;
    }
    
    @Override
    public String toString() {
        return "OrderEntity [id=" + id + ", sequenceId=" + sequenceId + ", direction=" + direction + ", userId="
                + userId + ", status=" + status + ", price=" + price + ", createdAt=" + createdAt + ", updatedAt="
                + updatedAt + ", version=" + version + ", quantity=" + quantity + ", unfilledQuantity="
                + unfilledQuantity + "]";
    }
    
    /**
     * 按OrderID排序
     */
    @Override
	public int compareTo(OrderEntity o) {
    	return Long.compare(this.id.longValue(), o.id.longValue());
	}    
}
