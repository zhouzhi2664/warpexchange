package com.zhoucong.exchange.support;

import org.springframework.beans.factory.annotation.Autowired;

import com.zhoucong.exchange.db.DbTemplate;

/**
 * Service with db support.
 */
public class AbstractDbService extends LoggerSupport {
	
	@Autowired
	protected DbTemplate db;
}
