package com.zhoucong.exchange.db;

import java.util.ArrayList;

/**
 * select ... from ... WHERE ...
 * 
 * @param <T> Generic type.
 */
public final class Where<T> extends CriteriaQuery<T> {
	
	Where(Criteria<T> criteria, String clause, Object... params){
		super(criteria);
		this.criteria.where = clause;
		this.criteria.whereParams = new ArrayList<>();
		// add:
        for (Object param : params) {
            this.criteria.whereParams.add(param);
        }
	}
	
	public OrderBy<T> orderBy(String orderBy) {
        return new OrderBy<>(this.criteria, orderBy);
    }
	
}
