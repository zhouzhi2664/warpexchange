package com.zhoucong.exchange.zcwpconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring config server. This project is only used to serve config files.
 * 
 * NOTE the default port of config server is 8888.
 */
@EnableConfigServer
@SpringBootApplication
public class ZCWPConfigApplication {
	
	public static void main(String[] args) {
        SpringApplication.run(ZCWPConfigApplication.class, args);
    }
}
