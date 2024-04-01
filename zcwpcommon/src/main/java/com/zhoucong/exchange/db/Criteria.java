package com.zhoucong.exchange.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold criteria query information.
 * 
 * @param <T> Entity type.
 */
public class Criteria<T> {
	
	DbTemplate db;
	Mapper<T> mapper;
    Class<T> clazz;
    List<String> select = null;
    String table = null;
    String where = null;
    List<Object> whereParams = null;
    List<String> orderBy = null;
    int offset = 0;
    int maxResults = 0;
	
	Criteria(DbTemplate db) {
        this.db = db;
    }
	
	String sql() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("SELECT ");
		sb.append((select == null ? "*" : String.join(", ", select)));
		sb.append(" FROM ").append(mapper.tableName);
		if (where != null) {
            sb.append(" WHERE ").append(String.join(" ", where));
        }
        if (orderBy != null) {
            sb.append(" ORDER BY ").append(String.join(", ", orderBy));
        }
        if (offset >= 0 && maxResults > 0) {
            sb.append(" LIMIT ?, ?");
        }
		String s = sb.toString();
		return s;
	}
	
	Object[] params() {
		List<Object> params = new ArrayList<>();
		if (where != null) {
			for (Object obj : whereParams) {
                if (obj == null) {
                    params.add(null);
                } else {
                    params.add(obj);
                }
            }
		}
		if (offset >= 0 && maxResults > 0) {
            params.add(offset);
            params.add(maxResults);
        }
		return params.toArray();	
	}
	
	List<T> list() {
		String selectSql = sql();
		Object[] selectParams = params();
		return db.jdbcTemplate.query(selectSql, mapper.resultSetExtractor, selectParams);
	}

}
