package com.zhoucong.exchange.match;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.zhoucong.exchange.model.trade.OrderEntity;

public class MatchResult {

	public final OrderEntity takerOrder;
    public final List<MatchDetailRecord> matchDetails = new ArrayList<>();
    
    public MatchResult(OrderEntity takerOrder) {
        this.takerOrder = takerOrder;
    }
    
    public void add(BigDecimal price, BigDecimal matchedQuantity, OrderEntity makerOrder) {
        matchDetails.add(new MatchDetailRecord(price, matchedQuantity, this.takerOrder, makerOrder));
    }

    @Override
    public String toString() {
    	return "...（Waiting to write）";
    }
}
