package com.zhoucong.exchange.store;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.zhoucong.exchange.model.support.EntitySupport;
import com.zhoucong.exchange.db.DbTemplate;
import com.zhoucong.exchange.messaging.MessageTypes;
import com.zhoucong.exchange.support.LoggerSupport;

public class StoreService extends LoggerSupport {

	@Autowired
    MessageTypes messageTypes;

    @Autowired
    DbTemplate dbTemplate;
    
    public void insertIgnore(List<? extends EntitySupport> list) {
    	//dbTemplate.insertIgnore(list);
    }
}
