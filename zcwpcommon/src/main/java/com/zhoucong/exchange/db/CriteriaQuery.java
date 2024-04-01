package com.zhoucong.exchange.db;

/**
 * Base criteria query.
 * 
 * @param <T> Generic type.
 */
public class CriteriaQuery<T> {
	
	protected final Criteria<T> criteria;
	
	CriteriaQuery(Criteria<T> criteria) {
        this.criteria = criteria;
    }
	
}
