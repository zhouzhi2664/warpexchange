package com.zhoucong.exchange.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * A simple ORM wrapper for JdbcTemplate.
 */
@Component
public class DbTemplate {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	final JdbcTemplate jdbcTemplate;
	
	public DbTemplate(@Autowired JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		String pkg = getClass().getPackageName();
        int pos = pkg.lastIndexOf(".");
        String basePackage = pkg.substring(0, pos) + ".model";
        
        List<Class<?>> classes = scanEntities(basePackage);
        
	}
}
