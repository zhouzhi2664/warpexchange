package com.zhoucong.exchange.db;

import com.itranswarp.exchange.db.From;

/**
 * SELECT ... from ...
 * 
 * Default to "*".
 */
@SuppressWarnings("unchecked")
public final class Select extends CriteriaQuery {
	
	Select(Criteria criteria, String... selectFields) {
		super(criteria);
		
	}
	
	/**
     * Add from clause.
     * 
     * @param entityClass The entity class.
     * @return The criteria object.
     */
    @SuppressWarnings("unchecked")
    public <T> From<T> from(Class<T> entityClass) {
        return new From<T>(this.criteria, this.criteria.db.getMapper(entityClass));
    }
}
