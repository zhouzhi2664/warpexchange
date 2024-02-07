package com.zhoucong.exchange.match;

import java.math.BigDecimal;

import com.zhoucong.exchange.model.trade.OrderEntity;

public record MatchDetailRecord(BigDecimal price, BigDecimal quantity, OrderEntity takerOrder, OrderEntity makerOrder) {

}
