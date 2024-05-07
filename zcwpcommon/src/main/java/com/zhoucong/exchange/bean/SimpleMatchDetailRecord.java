package com.zhoucong.exchange.bean;

import java.math.BigDecimal;

import com.zhoucong.exchange.enums.MatchType;

public record SimpleMatchDetailRecord(BigDecimal price, BigDecimal quantity, MatchType type) {
}
