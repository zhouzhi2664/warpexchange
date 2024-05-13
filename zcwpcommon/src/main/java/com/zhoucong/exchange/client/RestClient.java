package com.zhoucong.exchange.client;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;

/**
 * Http client for accessing exchange REST APIs.
 */
public class RestClient {

	final Logger logger = LoggerFactory.getLogger(getClass());
	
	public <T> T get(Class<T> clazz, String path, String authHeader, Map<String, String> query) {
        //TODO
        return null;
    }
	
    public <T> T get(TypeReference<T> ref, String path, String authHeader, Map<String, String> query) {
        //TODO
        return null;
    }
    
	public <T> T post(Class<T> clazz, String path, String authHeader, Object body) {
		//TODO
		return null;
	}
}
