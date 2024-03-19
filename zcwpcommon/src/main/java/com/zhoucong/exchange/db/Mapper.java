package com.zhoucong.exchange.db;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mapper<T> {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	final Class<T> entityClass;
    //final Constructor<T> constructor;
    final String tableName;
    
    // @Id property:
    final AccessibleProperty id;
    
    final List<AccessibleProperty> insertableProperties;
    final List<AccessibleProperty> updatableProperties;
	
	final String selectSQL;
    final String insertSQL;
    //final String insertIgnoreSQL;
    final String updateSQL;
    final String deleteSQL;

	
	public Mapper(Class<T> clazz) throws Exception {		
		
		this.tableName = getTableName(clazz);
		
		this.updateSQL = "UPDATE" + this.tableName + "SET"
				+ String.join(", ",
						this.updatableProperties.stream().map(p -> p.propertyName + " = ?").toArray(String[]::new))
				+" WHERE " + this.id.propertyName + " = ?";
	}


	private String getTableName(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
