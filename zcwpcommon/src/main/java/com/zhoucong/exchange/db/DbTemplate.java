package com.zhoucong.exchange.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;

/**
 * A simple ORM wrapper for JdbcTemplate.
 */
@Component
public class DbTemplate {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	final JdbcTemplate jdbcTemplate;
	
	// class -> Mapper:
    private Map<Class<?>, Mapper<?>> classMapping;
	
	public DbTemplate(@Autowired JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		String pkg = getClass().getPackageName();
        int pos = pkg.lastIndexOf(".");
        String basePackage = pkg.substring(0, pos) + ".model";
        
        List<Class<?>> classes = scanEntities(basePackage);
        Map<Class<?>, Mapper<?>> classMapping = new HashMap<>();
        try {
        	for(Class<?> clazz : classes) {
        		logger.info("Found class: " + clazz.getName());
        		Mapper<?> mapper = new Mapper<>(clazz);
        		
        		//TODO
        	}
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.classMapping = classMapping;
	}
	
	public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }
	
	
	
	private static List<Class<?>> scanEntities(String basePackage) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		List<Class<?>> classes = new ArrayList<>();
		Set<BeanDefinition> beans = provider.findCandidateComponents(basePackage);
		for (BeanDefinition bean : beans) {
			try {
				classes.add(Class.forName(bean.getBeanClassName()));
			} catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
		}
		return classes;
	}
}
