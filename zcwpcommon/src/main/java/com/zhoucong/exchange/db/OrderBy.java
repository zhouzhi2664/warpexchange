package com.zhoucong.exchange.db;

import java.util.ArrayList;

/**
 * select ... from ... ORDER BY ...
 * 
 * @param <T> Generic type.
 */
public class OrderBy<T> extends CriteriaQuery {
	
	public OrderBy(Criteria<T> criteria, String orderBy) {
        super(criteria);
        orderBy(orderBy);
    }
	
	/**
     * Order by field name.
     * 
     * @param orderBy The field name.
     * @return Criteria query object.
     */
	public OrderBy<T> orderBy(String orderBy){
		if (criteria.orderBy == null) {
            criteria.orderBy = new ArrayList<>();
        }
		criteria.orderBy.add(orderBy);
        return this;
	}
	
	/**
     * Add limit clause.
     * 
     * @param maxResults The max results.
     * @return Criteria query object.
     */
	public Limit<T> limit(int maxResults){
		return limit(0,maxResults);
	}
	
	public Limit<T> limit(int offset, int maxResults) {
        return new Limit<>(this.criteria, offset, maxResults);
    }
}
