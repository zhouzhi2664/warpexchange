package com.zhoucong.exchange.assets;

import java.math.BigDecimal;

public class Asset {
	    // 可用余额:
	    BigDecimal available;
	    // 冻结余额:
	    BigDecimal frozen;

	    public Asset() {
	        this(BigDecimal.ZERO, BigDecimal.ZERO);
	    }

	    public Asset(BigDecimal available, BigDecimal frozen) {
	        this.available = available;
	        this.frozen = frozen;
	    }
	    
	    public BigDecimal getAvailable() {
	        return available;
	    }

	    public BigDecimal getFrozen() {
	        return frozen;
	    }


}
