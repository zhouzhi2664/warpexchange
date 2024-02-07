package com.zhoucong.exchange.match;

import java.math.BigDecimal;

//以record实现的OrderKey:
public record OrderKey(long sequenceId, BigDecimal price) {
}