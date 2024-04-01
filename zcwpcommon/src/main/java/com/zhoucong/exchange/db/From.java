package com.zhoucong.exchange.db;

/**
 * select ... FROM ...
 *
 * @param <T> Generic type.
 */
public final class From<T> extends CriteriaQuery<T> {
	
	From(Criteria<T> criteria, Mapper<T> mapper) {
		super(criteria);
		this.criteria.mapper = mapper;
        this.criteria.clazz = mapper.entityClass;
        this.criteria.table = mapper.tableName;
	}
	
	/**
     * Add where clause.
     * 
     * @param clause Clause like "name = ?".
     * @param args   Arguments to match clause.
     * @return CriteriaQuery object.
     */
    public Where<T> where(String clause, Object... args) {
        return new Where<>(this.criteria, clause, args);
    }
}
