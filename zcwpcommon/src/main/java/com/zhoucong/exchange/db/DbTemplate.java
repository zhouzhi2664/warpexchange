package com.zhoucong.exchange.db;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.PersistenceException;

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
        		classMapping.put(clazz, mapper);
        	}
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.classMapping = classMapping;
	}
	
	public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }
	
	public <T> void insert(List<T> beans) {
        for (T bean : beans) {
            doInsert(bean, false);
        }
    }

    public <T> void insertIgnore(List<T> beans) {
        for (T bean : beans) {
            doInsert(bean, true);
        }
    }
    
	//TODO
	
	<T> void doInsert(T bean, boolean isIgnore) {
		try {
			int rows;
			final Mapper<?> mapper = getMapper(bean.getClass());
			Object[] args = new Object[mapper.insertableProperties.size()];
			int n = 0;
			for (AccessibleProperty prop : mapper.insertableProperties) {
                args[n] = prop.get(bean);
                n++;
            }
			if (logger.isDebugEnabled()) {
                logger.debug("SQL: {}", isIgnore ? mapper.insertIgnoreSQL : mapper.insertSQL);
            }
			if (mapper.id.isIdentityId()) {
				// using identityId:
				KeyHolder keyHolder = new GeneratedKeyHolder();
				rows = jdbcTemplate.update(new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps = connection.prepareStatement(
                                isIgnore ? mapper.insertIgnoreSQL : mapper.insertSQL, Statement.RETURN_GENERATED_KEYS);
						for (int i = 0; i < args.length; i++) {
                            ps.setObject(i + 1, args[i]);
                        }
                        return ps;
					}
				}, keyHolder);
				if (rows == 1) {
                    Number key = keyHolder.getKey();
                    if (key instanceof BigInteger) {
                        key = ((BigInteger) key).longValueExact();
                    }
                    mapper.id.set(bean, key);
                }				
			} else {
                // id is specified:
                rows = jdbcTemplate.update(isIgnore ? mapper.insertIgnoreSQL : mapper.insertSQL, args);
            }
			
		} catch (ReflectiveOperationException e) {
            throw new PersistenceException(e);
        }
	}
	
	// get mapper by class:
    @SuppressWarnings("unchecked")
    <T> Mapper<T> getMapper(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
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
