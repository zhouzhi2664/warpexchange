package com.zhoucong.exchange.model.support;

/**
 * Define entity support.
 */
public interface EntitySupport {
	
	/**
     * Default big decimal storage type: DECIMAL(PRECISION, SCALE)
     *
     * Range = +/-999999999999999999.999999999999999999
     */
    int PRECISION = 36;

    /**
     * Default big decimal storage scale. Minimum is 0.000000000000000001.
     */
    int SCALE = 18;
	int VAR_ENUM = 32;
}
